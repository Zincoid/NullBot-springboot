package com.zincoid.nullbot.bot.command.assist;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.exception.BotErrorException;
import com.zincoid.nullbot.bot.exception.BotInfoException;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.core.enums.Emoji;
import com.zincoid.nullbot.core.module.request.RequestClient;
import com.zincoid.nullbot.core.utils.ImgUtil;
import com.zincoid.nullbot.core.utils.MsgUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Base64;
import java.util.*;

@Slf4j
@CmdMapping({"Trace", "图溯源", "溯源"})
@Component
@RequiredArgsConstructor
public class TraceCmd implements Cmd {

    private static final String API_URL = "https://saucenao.com/search.php?output_type=2&db=999&numres=%s%s";

    private final RequestClient requestClient;

    @Value("${trace.saucenao.api-key:}")
    private String apiKey;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        int count = Math.clamp(args.getInt(0, 1), 1, 5);

        ArrayMsg first = event.getArrayMsg().getFirst();
        if (first.getType() != MsgTypeEnum.reply)
            throw new BotWarnException("缺少图片引用");

        Collection<String> urls = MsgUtil.extractImgMap(
                bot.getMsg((int) first.getLongData("id"))
                        .getData()
                        .getArrayMsg()
        ).values();
        if (urls.isEmpty())
            throw new BotWarnException("缺少图片引用");
        bot.sendGroupMsg(event.getGroupId(), "图像溯源中，请稍候...", false);
        String dataUri = ImgUtil.toDataUri(urls.iterator().next());
        if (dataUri == null) throw new BotWarnException("图片下载失败");
        String mime = dataUri.substring(5, dataUri.indexOf(';'));
        byte[] image = Base64.getDecoder().decode(dataUri.substring(dataUri.indexOf(',') + 1));
        String query = API_URL.formatted(count, apiKey.isBlank() ? "" : "&api_key=" + apiKey);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new NamedResource(image, "image." + mime.substring(6)));
        SauceResp resp = requestClient.post(query, body, MediaType.MULTIPART_FORM_DATA, SauceResp.class);

        if (resp == null || resp.header() == null)
            throw new BotErrorException("接口返回异常");
        if (resp.header().status() != 0)
            throw new BotWarnException(resp.header().status() < 0 ? "图无效或受限" : "服务端异常");
        if (resp.results() == null || resp.results().isEmpty())
            throw new BotInfoException(Emoji.INFO, "未找到匹配结果");

        for (SauceResult result : resp.results()) {
            SauceResultHeader header = result.header();
            SauceData data = result.data();
            if (header == null || data == null) continue;
            StringBuilder text = new StringBuilder("\n相似度: ").append(header.similarity()).append("%")
                    .append("\n来源: ").append(header.index_name())
                    .append("\n标题: ").append(data.titleOf())
                    .append("\n作者: ").append(data.authorOf());
            String url = SauceData.firstOf(data.ext_urls());
            if (url != null) text.append("\n链接: ").append(url);
            var builder = MsgUtils.builder();
            if (header.thumbnail() != null && !header.thumbnail().isBlank()) {
                String thumb = ImgUtil.toBase64(header.thumbnail());
                if (thumb != null) builder.img("base64://" + thumb);
                else builder.text("[图片未载入]");
            }
            bot.sendGroupMsg(event.getGroupId(), builder.text(text.toString()).build(), false);
        }
        log.info("☑ [Trace] 溯源完成 - 数量: {}", count);
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Trace 命令
                功能: 图片溯源 (SauceNAO)
                限权: %d 级
                格式: [引用图片] Trace [可选: 数量]
                数量: 1~5 (默认 1)
                别名: 图溯源/溯源""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return """
                ◉ Trace 命令
                功能: 通过 SauceNAO 以图搜源, 查找图片出处
                格式: Trace [数量] (需引用一张图片)
                数量: 1~5 (默认 1)
                示例: Trace 3""";
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SauceResp(SauceHeader header, List<SauceResult> results) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SauceHeader(Integer status) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SauceResult(SauceResultHeader header, SauceData data) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SauceResultHeader(String similarity, String thumbnail, String index_name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SauceData(Object ext_urls, Object title, Object member_name, Object author, Object creator) {
        private static String firstOf(Object value) {
            return switch (value) {
                case String s -> s;
                case List<?> list when !list.isEmpty() && list.getFirst() != null -> list.getFirst().toString();
                case null, default -> null;
            };
        }

        private String titleOf() {
            String t = firstOf(title);
            return t != null && !t.isBlank() ? t : "未知";
        }

        private String authorOf() {
            for (Object field : new Object[]{member_name, author, creator}) {
                String name = firstOf(field);
                if (name != null && !name.isBlank()) return name;
            }
            return "未知";
        }
    }

    private static final class NamedResource extends ByteArrayResource {
        private final String filename;

        private NamedResource(byte[] bytes, String filename) {
            super(bytes);
            this.filename = filename;
        }

        @Override
        public String getFilename() { return filename; }
    }
}
