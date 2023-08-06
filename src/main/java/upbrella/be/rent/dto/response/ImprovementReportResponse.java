package upbrella.be.rent.dto.response;

import lombok.*;
import upbrella.be.rent.entity.ImprovementReport;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ImprovementReportResponse {

    private long id;
    private long umbrellaId;
    private String content;
    private String etc;

    public static ImprovementReportResponse fromImprovementReport(ImprovementReport improvementReport) {

        return ImprovementReportResponse.builder()
                .id(improvementReport.getId())
                .umbrellaId(improvementReport.getHistory().getId())
                .content(improvementReport.getContent())
                .etc(improvementReport.getEtc())
                .build();
    }
}
