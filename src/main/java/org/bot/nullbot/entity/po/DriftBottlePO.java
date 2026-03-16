package org.bot.nullbot.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bot.nullbot.command.game.single.DriftBottleCommand;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("`drift_bottle`")
public class DriftBottlePO
{
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private LocalDateTime time;
    private Long userId;
    private String userName;
    private String text;
    private Integer rethrowTimes;

    public void plusRethrowTimes() {
        rethrowTimes++;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedTime = time != null ? time.format(formatter) : "";
        return """
            [%ss后销毁 - 发送"扔回去"投回]
            漂流瓶 #%d (%s)
            时间: %s

            %s

            —— %s(%d)"""
                .formatted(
                        DriftBottleCommand.getKeepTime(),
                        id,
                        rethrowTimes == 0 ? "首次被捡到" : "已被投回 " + rethrowTimes + " 次",
                        formattedTime,
                        text,
                        userName,
                        userId
                );
    }
}
