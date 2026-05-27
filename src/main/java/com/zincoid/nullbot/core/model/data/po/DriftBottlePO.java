package com.zincoid.nullbot.core.model.data.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mikuac.shiro.common.utils.MsgUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.zincoid.nullbot.bot.command.game.single.DriftBottleCommand;
import com.zincoid.nullbot.core.util.Base64Util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("`drift_bottle`")
public class DriftBottlePO {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private LocalDateTime time;
    private Long userId;
    private String userName;
    private String content;  // 图片类型为本地文件路径
    private Boolean isImage;
    private Integer rethrowTimes;

    public void plusRethrowTimes() {
        rethrowTimes++;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedTime = time != null ? time.format(formatter) : "";
        MsgUtils builder = MsgUtils.builder();
        builder.text("""
            [%ss后销毁 - 发送"扔回去"投回]
            漂流瓶 #%d (%s)
            时间: %s
            
            """
                .formatted(
                        DriftBottleCommand.getKeepTimeoutSeconds(),
                        id,
                        rethrowTimes == 0 ? "首次被捡到" : "已被投回 " + rethrowTimes + " 次",
                        formattedTime
                )
        );
        if (isImage) {
            builder.img("base64://" + Base64Util.from(content));
        } else {
            builder.text(content + "\n");
        }
        builder.text("""
                
                —— %s(%d)""".formatted(userName, userId));
        return builder.build();
    }
}
