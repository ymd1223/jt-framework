package io.github.hylexus.jt808.samples.annotation.handler;

import io.github.hylexus.jt.annotation.msg.handler.Jt808ExceptionHandler;
import io.github.hylexus.jt.annotation.msg.handler.Jt808RequestMsgHandler;
import io.github.hylexus.jt.annotation.msg.handler.Jt808RequestMsgHandlerMapping;
import io.github.hylexus.jt.data.msg.BuiltinJt808MsgType;
import io.github.hylexus.jt808.msg.RequestMsgHeader;
import io.github.hylexus.jt808.msg.RequestMsgMetadata;
import io.github.hylexus.jt808.msg.RespMsgBody;
import io.github.hylexus.jt808.msg.req.BuiltinEmptyRequestMsgBody;
import io.github.hylexus.jt808.msg.req.BuiltinTerminalCommonReplyMsgBody;
import io.github.hylexus.jt808.msg.resp.CommonReplyMsgBody;
import io.github.hylexus.jt808.msg.resp.VoidRespMsgBody;
import io.github.hylexus.jt808.samples.annotation.entity.req.AuthRequestMsgBody;
import io.github.hylexus.jt808.samples.annotation.entity.req.LocationUploadRequestMsgBody;
import io.github.hylexus.jt808.samples.annotation.entity.req.PassthroughPack;
import io.github.hylexus.jt808.samples.annotation.entity.resp.ServerCommonReplyMsgBody;
import io.github.hylexus.jt808.session.Session;
import lombok.extern.slf4j.Slf4j;

import static io.github.hylexus.jt808.samples.annotation.config.Jt808MsgType.CLIENT_LOCATION_INFO_UPLOAD;

/**
 * @author hylexus
 * Created At 2020-02-01 2:54 下午
 */
@Slf4j
@Jt808RequestMsgHandler
public class CommonHandler {

    // 此处会覆盖内置的鉴权消息处理器(如果启用了的话)
    @Jt808RequestMsgHandlerMapping(msgType = 0x0102)
    public RespMsgBody processAuthMsg(AuthRequestMsgBody msgBody, RequestMsgHeader header) {
        log.info("处理鉴权消息 terminalId = {}, authCode = {}", header.getTerminalId(), msgBody.getAuthCode());
        if (header.getTerminalId().equals(System.getProperty("debug-terminal-id"))) {
            throw new UnsupportedOperationException("terminal [" + header.getTerminalId() + "] was locked.");
        }
        return CommonReplyMsgBody.success(header.getFlowId(), BuiltinJt808MsgType.CLIENT_AUTH);
    }

    @Jt808ExceptionHandler
    public RespMsgBody processUnsupportedOperationException(RequestMsgMetadata metadata, Session session, UnsupportedOperationException exception) {
        assert metadata.getHeader().getTerminalId().equals(session.getTerminalId());
        log.error("出异常了:{}", exception.getMessage());
        return VoidRespMsgBody.NO_DATA_WILL_BE_SENT_TO_CLIENT;
    }

    @Jt808ExceptionHandler
    public RespMsgBody processException(RequestMsgMetadata metadata, Session session, Exception exception) {
        assert metadata.getHeader().getTerminalId().equals(session.getTerminalId());
        log.info("exception", exception);
        return VoidRespMsgBody.NO_DATA_WILL_BE_SENT_TO_CLIENT;
    }

    // 处理MsgId为0x0200的消息
    @Jt808RequestMsgHandlerMapping(msgType = 0x0200)
    public ServerCommonReplyMsgBody processLocationMsg(
            Session session, RequestMsgMetadata metadata,
            RequestMsgHeader header, LocationUploadRequestMsgBody msgBody) {

        assert header.getMsgId() == BuiltinJt808MsgType.CLIENT_LOCATION_INFO_UPLOAD.getMsgId();
        assert session.getTerminalId().equals(header.getTerminalId());
        assert session.getTerminalId().equals(metadata.getHeader().getTerminalId());
        assert metadata.getHeader() == header;

        log.info("处理位置上报消息 terminalId = {}, msgBody = {}", header.getTerminalId(), msgBody);
        // return CommonReplyMsgBody.success(header.getFlowId(), BuiltinJt808MsgType.CLIENT_LOCATION_INFO_UPLOAD);
        return new ServerCommonReplyMsgBody(header.getFlowId(), CLIENT_LOCATION_INFO_UPLOAD.getMsgId(), (byte) 0);
    }

    @Jt808RequestMsgHandlerMapping(msgType = 0x0900)
    public RespMsgBody process0x0900(PassthroughPack body) {
        log.info("bug-fix --> 0x0900: {}", body);
        return VoidRespMsgBody.NO_DATA_WILL_BE_SENT_TO_CLIENT;
    }

    // 此处会覆盖内置的终端通用应答消息处理器(如果启用了的话)
    @Jt808RequestMsgHandlerMapping(msgType = 0x0001)
    public void processTerminalCommonReplyMsg(Session session, BuiltinTerminalCommonReplyMsgBody msgBody) {
        log.info("处理终端通用应答消息 terminalId = {}, msgBody = {}", session.getTerminalId(), msgBody);
    }

    @Jt808RequestMsgHandlerMapping(msgType = 0x0002)
    public void processTerminalHearBeatMsg(BuiltinEmptyRequestMsgBody heartBeatMsgBody) {
        log.info("bug-fix --> https://github.com/hylexus/jt-framework/issues/7 : {}", heartBeatMsgBody);
    }
}
