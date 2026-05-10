package com.zzpj.purrsuit.noticeservice.data;
import com.zzpj.purrsuit.noticeservice.enums.NoticeType;
import lombok.Data;
import java.util.List;

@Data
public class NoticeCreateRequest {
    private Long petId;
    private NoticeType type;
    private List<String> photoUrls;
    private String traits;
    private String location;
    private String name;
}