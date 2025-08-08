package com.petrece.ai_assistant.app;


import com.petrece.ai_assistant.advisor.MyLoggerAdvisor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Slf4j
@Component
public class AiApp {

    public final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "你是一位资深ERP系统专家，专注于企业资源计划系统的选型、实施、优化与变革管理。你的专业领域涵盖SAP、Oracle、用友、金蝶及主流云ERP平台，熟悉制造、零售、物流、服务等行业的业务流程数字化。" +
            "你的任务是帮助用户诊断ERP相关问题，提供专业、务实、可落地的建议。" +
            "当用户提问时，你需要：\n" +
            "1. 首先判断问题所属阶段：选型评估、项目实施、系统使用、运维优化或组织变革；\n" +
            "2. 若信息不足，主动引导用户补充关键背景：行业类型、企业规模、ERP系统名称（如有）、涉及部门、具体痛点；\n" +
            "3. 分析问题根源，结合行业最佳实践（如APQC流程框架、TOGAF架构方法、Change Management模型）给出结构化建议；\n" +
            "4. 输出语言必须专业、清晰、简洁，避免术语堆砌，必要时用比喻帮助理解；\n" +
            "5. 不假设用户具备技术背景，始终以业务价值为导向进行解释；\n" +
            "6. 禁止虚构案例或数据，不推荐具体厂商产品，保持中立客观；\n" +
            "7. 如遇超出ERP范畴的问题（如纯IT开发、财务做账），应礼貌说明边界并建议寻求专业支持。\n" +
            "你的语气应保持专业而亲和，像一位经验丰富的顾问在一对一咨询。现在开始，等待用户描述他们的ERP挑战。";;

    record AiReport(String title , List<String> suggestions){

    }

    public AiApp (ChatModel dashScopeChatModel){
        chatClient = ChatClient.builder(dashScopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(new InMemoryChatMemory()),
                        new MyLoggerAdvisor()
                )
                .build();
    }

    /**
     * 支持多轮对话
     * @param question
     * @param chatId
     * @return
     */
    public String doChat(String question, String chatId){
        ChatResponse chatResponse = chatClient.prompt().
                user(question)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10)).call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * Ai助理报告功能（实战结构化输出）
     * @param question
     * @param chatId
     * @return
     */
    public AiReport doChatWithReport(String question, String chatId){
        AiReport aiReport = chatClient.prompt().
                user(question)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(AiReport.class);
        log.info("aiReport: {}", aiReport);
        return aiReport;
    }
}