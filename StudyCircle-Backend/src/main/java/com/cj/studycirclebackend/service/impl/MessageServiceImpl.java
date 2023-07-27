package com.cj.studycirclebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cj.studycirclebackend.pojo.Message;
import com.cj.studycirclebackend.vo.MessageVO;
import com.cj.studycirclebackend.dao.MessageMapper;
import com.cj.studycirclebackend.dto.Response;
import com.cj.studycirclebackend.dto.ai.AIMessage;
import com.cj.studycirclebackend.dto.ai.AIResponse;
import com.cj.studycirclebackend.enums.MessageRole;
import com.cj.studycirclebackend.service.MessageService;
import com.cj.studycirclebackend.util.AIChatUtil;
import com.cj.studycirclebackend.util.DataUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {
    private static final Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);
    @Resource
    private AIChatUtil aiChatUtil;

    @Override
    public Response createMessage(Long conversationId, Long messageTargetId, String question) {
        List<AIMessage> messages = new ArrayList<>();
        // 短文本历史记录

        messages.add(new AIMessage(MessageRole.USER.getValue(), question));
        AIResponse aiResponse = aiChatUtil.createChatCompletion(messages);
        String content = aiResponse.getChoices().get(0).getMessage().getContent();
        long promptTokens = aiResponse.getUsage().getPromptTokens();
        long completionTokens = aiResponse.getUsage().getCompletionTokens();

        List<MessageVO> messageVOList = new ArrayList<>();
        // 自身对话
        Message userMessage = new Message();
        userMessage.setConversationId(conversationId);
        userMessage.setContent(question);
        userMessage.setMessageTargetId(messageTargetId);
        userMessage.setSendTime(new Date());
        userMessage.setRole(MessageRole.USER.getValue());
        userMessage.setIsDeleted(0);
        userMessage.setTokens(promptTokens);
        save(userMessage);
        MessageVO userMessageVO = getMessageVO(userMessage);

        // 对方对话
        Message aiMessage = new Message();
        aiMessage.setConversationId(conversationId);
        aiMessage.setContent(content);
        aiMessage.setMessageTargetId(userMessage.getId()); // 设置对话 id
        aiMessage.setSendTime(DataUtil.afterRandomSeconds(userMessage.getSendTime()));
        aiMessage.setRole(MessageRole.ASSISTANT.getValue());
        aiMessage.setIsDeleted(0);
        aiMessage.setTokens(completionTokens);
        save(aiMessage);
        MessageVO aiMessageVO = getMessageVO(aiMessage);

        messageVOList.add(userMessageVO);
        messageVOList.add(aiMessageVO);
        return Response.ok(messageVOList);
    }

    @Override
    public MessageVO getMessageVO(Message message) {
        MessageVO messageVO = new MessageVO();
        messageVO.setMessageId(message.getId());
        messageVO.setContent(message.getContent());
        messageVO.setSendTime(DataUtil.formatDateTime(message.getSendTime()));
        messageVO.setRole(message.getRole());
        return messageVO;
    }



    @Override
    public Response deleteMessage(Long messageId) {
        boolean res = update(new UpdateWrapper<Message>().set("is_deleted", 1).eq("id", messageId));
        return res ? Response.ok() : Response.notContent();
    }

    @Override
    public Response updateMessage(Long messageId) {
        List<AIMessage> messages = new ArrayList<>();
        Message message = getById(messageId);
        if (message == null) {
            return Response.notContent();
        }
        String question = getById(message.getMessageTargetId()).getContent();
        // 历史会话
        messages.add(new AIMessage("user", question));
        AIResponse aiResponse = aiChatUtil.createChatCompletion(messages);
        String content = aiResponse.getChoices().get(0).getMessage().getContent();
        long tokens = aiResponse.getUsage().getCompletionTokens();
        message.setContent(content);
        message.setTokens(tokens);
        boolean res = saveOrUpdate(message);

        return res ? Response.ok(getMessageVO(message)) : Response.internalServerError();
    }

    @Override
    public Response getMessage(Long conversationId) {
        List<MessageVO> res = getMessageVOList(conversationId);
        return res != null ? Response.ok() : Response.notContent();
    }

    @Override
    public List<MessageVO> getMessageVOList(Long conversationId) {
        List<Message> messages = list(new QueryWrapper<Message>().
                eq("conversation_id", conversationId)
                .eq("is_deleted", 0)
                .orderByAsc("send_time"));
        if (messages == null) {
            return null;
        }
        return getMessageVOList(messages);
    }
    @Override
    public List<MessageVO> getMessageVOList(List<Message> messageList) {
        List<MessageVO> messageVOList = new ArrayList<>(messageList.size());
        for (Message message : messageList) {
            messageVOList.add(getMessageVO(message));
        }
        return messageVOList;
    }
}
