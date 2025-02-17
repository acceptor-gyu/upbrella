package upbrella.be.store.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import upbrella.be.store.entity.BusinessHour;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@Builder
@AllArgsConstructor
public class SingleBusinessHourResponse {

    private DayOfWeek date;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime openAt;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime closeAt;

    public static SingleBusinessHourResponse createSingleHourResponse(BusinessHour businessHour) {

            return SingleBusinessHourResponse.builder()
                    .date(businessHour.getDate())
                    .openAt(businessHour.getOpenAt())
                    .closeAt(businessHour.getCloseAt())
                    .build();
    }
}
