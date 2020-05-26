
package io.github.hylexus.jt808.samples.annotation.entity.req;

import io.github.hylexus.jt.annotation.msg.req.Jt808ReqMsgBody;
import io.github.hylexus.jt.annotation.msg.req.basic.BasicField;
import io.github.hylexus.jt.annotation.msg.req.extra.ExtraField;
import io.github.hylexus.jt.annotation.msg.req.extra.ExtraMsgBody;
import io.github.hylexus.jt.annotation.msg.req.slice.SlicedFrom;
import io.github.hylexus.jt.annotation.msg.req.slice.SplittableField;
import io.github.hylexus.jt.data.converter.req.entity.LngLatReqMsgFieldConverter;
import io.github.hylexus.jt808.msg.RequestMsgBody;
import io.github.hylexus.jt808.msg.RequestMsgMetadata;
import io.github.hylexus.jt808.support.entity.scan.RequestMsgMetadataAware;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static io.github.hylexus.jt.data.MsgDataType.*;

/**
 * @author hylexus
 * Created At 2020-01-29 12:03 下午
 */
@Slf4j
@Data
@Accessors(chain = true)
@Jt808ReqMsgBody(msgType = 0x0200)
public class LocationUploadRequestMsgBody implements RequestMsgBody, RequestMsgMetadataAware {

    @ToString.Exclude
    private RequestMsgMetadata requestMsgMetadata;

    @Override
    public void setRequestMsgMetadata(RequestMsgMetadata metadata) {
        this.requestMsgMetadata = metadata;
    }

    // 报警标志
    @BasicField(startIndex = 0, dataType = DWORD)
    private int alarmFlag;

    // 状态
    @BasicField(startIndex = 4, dataType = DWORD)
    @SplittableField(splitPropertyValueIntoNestedBeanField = "statusInfo")
    private int status;

    private LocationUploadStatus statusInfo;

    @BasicField(startIndex = 4, dataType = BYTES, length = 4)
    private byte[] statusBytes;

    // 将上面的 status 字段的第0位取出转为 int 类型
    @SlicedFrom(sourceFieldName = "status", bitIndex = 0)
    private int accIntStatus;

    // 将上面的 status 字段的第0位取出转为 boolean 类型
    @SlicedFrom(sourceFieldName = "status", bitIndex = 0)
    private Boolean accBooleanStatus;

    // 0 北纬;1 南纬
    // 将上面的 status 字段的第2位取出转为 int 类型
    @SlicedFrom(sourceFieldName = "status", bitIndex = 2)
    private int latType;

    // 纬度(尚未除以 10^6)
    @BasicField(startIndex = 8, dataType = DWORD)
    private Integer intLat;
    // 纬度(使用转换器除以10^6转为Double类型)
    @BasicField(startIndex = 8, dataType = DWORD, customerDataTypeConverterClass = LngLatReqMsgFieldConverter.class)
    private Double lat;

    // 经度(尚未除以 10^6)
    @BasicField(startIndex = 12, dataType = DWORD)
    private Integer intLng;
    // 经度(使用转换器除以10^6转为Double类型)
    @BasicField(startIndex = 12, dataType = DWORD, customerDataTypeConverterClass = LngLatReqMsgFieldConverter.class)
    private Double lng;

    // 经度(startIndexMethod使用示例)
    @BasicField(startIndexMethod = "getLngStartIndex", dataType = DWORD, customerDataTypeConverterClass = LngLatReqMsgFieldConverter.class)
    private Double lngByStartIndexMethod;

    // a sample for https://github.com/hylexus/jt-framework/issues/9
    public int getLngStartIndex() {
        log.info("消息体总长度:{}", this.requestMsgMetadata.getHeader().getMsgBodyLength());
        return 12;
    }

    // 高度
    @BasicField(startIndex = 16, dataType = WORD)
    private Integer height;

    // 速度
    @BasicField(startIndex = 18, dataType = WORD)
    private int speed;

    // 方向
    @BasicField(startIndex = 20, dataType = WORD)
    private Integer direction;

    @BasicField(startIndex = 22, dataType = BCD, length = 6)
    private String time;

    @ExtraField(
            // 消息体中第28字节开始
            startIndex = 28,
            // 附加项长度用getExtraInfoLength返回值表示
            byteCountMethod = "getExtraInfoLength"
    )
    private ExtraInfo extraInfo;

    public int getExtraInfoLength() {
        int totalLength = this.requestMsgMetadata.getHeader().getMsgBodyLength();
        return totalLength - 28;
    }

    // 也可将将附加项解析为一个List
    @BasicField(startIndex = 28,byteCountMethod = "getExtraInfoLength",dataType = LIST)
    private List<ExtraInfoItem> extraInfoItemList;

    @Data
    // 切记@ExtraMsgBody注解不能丢
    @ExtraMsgBody(
            byteCountOfMsgId = 1, // 消息Id用1个字节表示
            byteCountOfContentLength = 1 // 附加项长度字段用1个字节表示
    )
    public static class ExtraInfo {
        @ExtraField.NestedFieldMapping(msgId = 0x30, dataType = BYTE)
        private int field0x30;

        @ExtraField.NestedFieldMapping(msgId = 0x31, dataType = BYTE)
        private int field0x31;

        @ExtraField.NestedFieldMapping(msgId = 0xe2, dataType = WORD)
        private int field0xe2;

        @ExtraField.NestedFieldMapping(msgId = 0xe1, dataType = DWORD)
        private int field0xe1;

        @ExtraField.NestedFieldMapping(msgId = 0xE3, dataType = BYTES)
        private byte[] field0xe3;

        @ExtraField.NestedFieldMapping(msgId = 0xE4, dataType = BYTES)
        private byte[] field0xe4;

        @ExtraField.NestedFieldMapping(msgId = 0xE5, dataType = DWORD)
        private int field0xe5;

        @ExtraField.NestedFieldMapping(msgId = 0xE6, dataType = BYTE)
        private byte field0xe6;

        @ExtraField.NestedFieldMapping(msgId = 0x53,dataType = BYTES)
        private byte[] field0x53;
    }

    @Data
    public static class LocationUploadStatus {
        @SplittableField.BitAt(bitIndex = 0)
        private boolean accStatus; // acc开?
        @SplittableField.BitAt(bitIndex = 1)
        private int bit1; //1:定位, 0:未定义
        @SplittableField.BitAt(bitIndex = 2)
        private Boolean isSouthLat;// 是否南纬?
        @SplittableField.BitAt(bitIndex = 3)
        private Integer lngType;
        // 将第0位和第1位同时取出并转为int
        // 在此处无实际意义,只是演示可以这么使用
        @SplittableField.BitAtRange(startIndex = 0, endIndex = 1)
        private int bit0to1;
    }
}
