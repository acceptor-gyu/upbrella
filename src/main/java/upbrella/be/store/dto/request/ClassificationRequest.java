package upbrella.be.store.dto.request;

import com.sun.istack.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ClassificationRequest {

    @NotNull
    private long id;
    @NotBlank
    private String type;
    @NotBlank
    private String name;
    @Range(min = -90, max = 90)
    private double latitude;
    @Range(min = -180, max = 180)
    private double longitude;
}