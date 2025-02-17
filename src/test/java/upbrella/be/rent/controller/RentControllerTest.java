package upbrella.be.rent.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import upbrella.be.docs.utils.RestDocsSupport;
import upbrella.be.rent.dto.request.RentUmbrellaByUserRequest;
import upbrella.be.rent.dto.request.ReturnUmbrellaByUserRequest;
import upbrella.be.rent.dto.response.*;
import upbrella.be.rent.service.ConditionReportService;
import upbrella.be.rent.service.RentService;
import upbrella.be.user.entity.User;
import upbrella.be.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static upbrella.be.docs.utils.ApiDocumentUtils.getDocumentRequest;
import static upbrella.be.docs.utils.ApiDocumentUtils.getDocumentResponse;

@ExtendWith(MockitoExtension.class)
public class RentControllerTest extends RestDocsSupport {

    @Mock
    private ConditionReportService conditionReportService;
    @Mock
    private RentService rentService;
    @Mock
    private UserRepository userRepository;

    @Override
    protected Object initController() {
        return new RentController(conditionReportService, rentService, userRepository);
    }

    @DisplayName("사용자는 우산 대여 요청을 할 수 있다.")
    @Test
    void rentUmbrellaTest() throws Exception {

        RentUmbrellaByUserRequest request = RentUmbrellaByUserRequest.builder()
                .region("신촌")
                .storeId(1L)
                .umbrellaId(1L)
                .conditionReport("필요하다면 상태 신고를 해주세요.")
                .build();

        User newUser = User.builder()
                        .id(1L)
                        .name("테스터1")
                        .phoneNumber("010-1111-1111")
                        .adminStatus(false)
                        .build();


        given(userRepository.findById(1L)).willReturn(Optional.of(newUser));

        mockMvc.perform(
                        post("/rent")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("rent-umbrella-doc",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("region").type(JsonFieldType.STRING)
                                        .description("지역"),
                                fieldWithPath("storeId").type(JsonFieldType.NUMBER)
                                        .description("협업 지점 고유번호"),
                                fieldWithPath("umbrellaId").type(JsonFieldType.NUMBER)
                                        .description("우산 고유번호"),
                                fieldWithPath("conditionReport").type(JsonFieldType.STRING)
                                        .optional()
                                        .description("상태 신고"))
                        ));
    }

    @DisplayName("사용자는 우산 반납 요청을 할 수 있다.")
    @Test
    void returnUmbrellaTest() throws Exception {
        ReturnUmbrellaByUserRequest request = ReturnUmbrellaByUserRequest.builder()
                .uuid(1)
                .storeId(1)
                .improvement("불편하셨다면 개선 사항을 입력해주세요.")
                .build();

        mockMvc.perform(
                        patch("/rent")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("return-umbrella-doc",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("uuid").type(JsonFieldType.NUMBER)
                                        .description("우산 고유번호"),
                                fieldWithPath("storeId").type(JsonFieldType.NUMBER)
                                        .description("협업 지점 고유번호"),
                                fieldWithPath("improvement").type(JsonFieldType.STRING)
                                        .optional()
                                        .description("개선 사항"))
                        ));
    }

    @DisplayName("사용자는 우산 대여 내역을 조회 할 수 있다.")
    @Test
    void showAllRentalHistoriesTest() throws Exception {

        RentalHistoriesPageResponse response = RentalHistoriesPageResponse.builder()
                .rentalHistoryResponsePage(List.of(RentalHistoryResponse.builder()
                        .id(1L)
                        .name("사용자")
                        .phoneNumber("010-1234-5678")
                        .rentStoreName("대여점 이름")
                        .rentAt(LocalDateTime.of(2023, 7, 18, 0, 0, 0))
                        .elapsedDay(3)
                        .umbrellaUuid(30)
                        .returnStoreName("반납점 이름")
                        .returnAt(LocalDateTime.now())
                        .totalRentalDay(5)
                        .refundCompleted(true)
                        .etc("기타")
                        .build())
                ).build();

        mockMvc.perform(
                        get("/rent/histories")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("show-all-rental-histories-doc",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("rentalHistoryResponsePage").type(JsonFieldType.ARRAY)
                                        .description("대여 내역 페이지 목록"),
                                fieldWithPath("rentalHistoryResponsePage[].id").type(JsonFieldType.NUMBER)
                                        .description("대여 내역 고유번호"),
                                fieldWithPath("rentalHistoryResponsePage[].name").type(JsonFieldType.STRING)
                                        .description("사용자 이름"),
                                fieldWithPath("rentalHistoryResponsePage[].phoneNumber").type(JsonFieldType.STRING)
                                        .description("사용자 전화번호"),
                                fieldWithPath("rentalHistoryResponsePage[].rentStoreName").type(JsonFieldType.STRING)
                                        .description("대여 지점 이름"),
                                fieldWithPath("rentalHistoryResponsePage[].rentAt")
                                        .description("대여 시간"),
                                fieldWithPath("rentalHistoryResponsePage[].elapsedDay").type(JsonFieldType.NUMBER)
                                        .description("대여 기간"),
                                fieldWithPath("rentalHistoryResponsePage[].umbrellaId").type(JsonFieldType.NUMBER)
                                        .description("우산 고유번호"),
                                fieldWithPath("rentalHistoryResponsePage[].returnStoreName").type(JsonFieldType.STRING)
                                        .optional()
                                        .description("반납 지점 이름"),
                                fieldWithPath("rentalHistoryResponsePage[].returnAt")
                                        .optional()
                                        .description("반납 시간"),
                                fieldWithPath("rentalHistoryResponsePage[].totalRentalDay").type(JsonFieldType.NUMBER)
                                        .optional()
                                        .description("총 대여 기간"),
                                fieldWithPath("rentalHistoryResponsePage[].refundCompleted").type(JsonFieldType.BOOLEAN)
                                        .description("환불 완료 여부"),
                                fieldWithPath("rentalHistoryResponsePage[].etc").type(JsonFieldType.STRING)
                                        .optional()
                                        .description("기타 사항")
                        )));
    }

    @DisplayName("사용자는 신고 내역을 조회할 수 있다.")
    @Test
    void showAllStatusConditionTest() throws Exception {

        List<ConditionReportResponse> response = List.of(ConditionReportResponse.builder()
                        .id(1L)
                        .umbrellaId(1L)
                        .content("우산이 망가졌습니다.")
                        .build());

        given(conditionReportService.findAllConditionReport()).willReturn(response);

        mockMvc.perform(
                        get("/rent/histories/status")
                ).andDo(print())
                .andExpect(status().isOk())
                .andDo(document("show-all-condition-reports-doc",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("conditionReports").type(JsonFieldType.ARRAY)
                                        .description("신고 내역 페이지"),
                                fieldWithPath("conditionReports[].id").type(JsonFieldType.NUMBER)
                                        .description("신고 내역 고유번호"),
                                fieldWithPath("conditionReports[].umbrellaId").type(JsonFieldType.NUMBER)
                                        .description("우산 고유번호"),
                                fieldWithPath("conditionReports[].content").type(JsonFieldType.STRING)
                                        .description("신고 내용"),
                                fieldWithPath("conditionReports[].etc").type(JsonFieldType.STRING)
                                        .optional()
                                        .description("기타 사항")
                        )));

    }

    @DisplayName("사용자는 개선 요청 내역을 조회할 수 있다.")
    @Test
    void showAllImprovementsTest() throws Exception {

        ImprovementReportPageResponse response = ImprovementReportPageResponse.builder()
                .improvementReports(List.of(ImprovementReportResponse.builder()
                        .id(1L)
                        .umbrellaId(1)
                        .content("정상적인 시기에 반납하기가 어려울 떈 어떻게 하죠?")
                        .etc("기타 사항")
                        .build())
                ).build();

        mockMvc.perform(
                        get("/rent/histories/improvements")
                ).andDo(print())
                .andExpect(status().isOk())
                .andDo(document("show-all-improvements-doc",
                        getDocumentResponse(),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("improvementReports").type(JsonFieldType.ARRAY)
                                        .description("개선 요청 목록"),
                                fieldWithPath("improvementReports[].id").type(JsonFieldType.NUMBER)
                                        .description("개선 요청 고유번호"),
                                fieldWithPath("improvementReports[].umbrellaId").type(JsonFieldType.NUMBER)
                                        .description("우산 고유번호"),
                                fieldWithPath("improvementReports[].content").type(JsonFieldType.STRING)
                                        .description("개선 요청 내용"),
                                fieldWithPath("improvementReports[].etc").type(JsonFieldType.STRING)
                                        .optional()
                                        .description("기타 사항")
                        )));

    }
}