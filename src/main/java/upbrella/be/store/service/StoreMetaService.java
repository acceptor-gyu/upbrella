package upbrella.be.store.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upbrella.be.store.dto.request.CoordinateRequest;
import upbrella.be.store.dto.request.CreateStoreRequest;
import upbrella.be.store.dto.request.SingleBusinessHourRequest;
import upbrella.be.store.dto.response.AllCurrentLocationStoreResponse;
import upbrella.be.store.dto.response.CurrentUmbrellaStoreResponse;
import upbrella.be.store.dto.response.SingleCurrentLocationStoreResponse;
import upbrella.be.store.entity.BusinessHour;
import upbrella.be.store.entity.Classification;
import upbrella.be.store.entity.StoreDetail;
import upbrella.be.store.entity.StoreMeta;
import upbrella.be.store.repository.StoreDetailRepository;
import upbrella.be.store.repository.StoreMetaRepository;
import upbrella.be.umbrella.entity.Umbrella;
import upbrella.be.umbrella.repository.UmbrellaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreMetaService {

    private final UmbrellaRepository umbrellaRepository;
    private final StoreMetaRepository storeMetaRepository;
    private final StoreDetailRepository storeDetailRepository;
    private final ClassificationService classificationService;
    private final BusinessHourService businessHourService;

    @Transactional(readOnly = true)
    public CurrentUmbrellaStoreResponse findCurrentStoreIdByUmbrella(long umbrellaId) {

        Umbrella foundUmbrella = umbrellaRepository.findByIdAndDeletedIsFalse(umbrellaId)
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 존재하지 않는 우산입니다."));

        if (foundUmbrella.getStoreMeta().isDeleted()) {
            throw new IllegalArgumentException("[ERROR] 삭제된 가게입니다.");
        }
        return CurrentUmbrellaStoreResponse.fromUmbrella(foundUmbrella);
    }

    public AllCurrentLocationStoreResponse findStoresInCurrentMap(CoordinateRequest coordinateRequest, LocalDateTime currentTime) {

        return AllCurrentLocationStoreResponse.ofCreate(findAllStores(coordinateRequest, currentTime));
    }

    private List<SingleCurrentLocationStoreResponse> findAllStores(CoordinateRequest coordinateRequest, LocalDateTime currentTime) {

        List<StoreMeta> storeMetaListInCurrentMap = storeMetaRepository.findAllByDeletedIsFalseAndLatitudeBetweenAndLongitudeBetween(
                coordinateRequest.getLatitudeFrom(), coordinateRequest.getLatitudeTo(),
                coordinateRequest.getLongitudeFrom(), coordinateRequest.getLongitudeTo()
        );

        return storeMetaListInCurrentMap.stream()
                .map(storeMeta -> mapToSingleCurrentLocationStoreResponse(storeMeta, currentTime))
                .collect(Collectors.toList());
    }

    private boolean isOpenStore(StoreMeta storeMeta, LocalDateTime currentTime) {

        Set<BusinessHour> businessHours = storeMeta.getBusinessHours();

        return businessHours.stream()
                .filter(businessHour -> businessHour.getDate().equals(currentTime.getDayOfWeek()))
                .filter(e -> storeMeta.isActivated())
                .anyMatch(businessHour ->
                        currentTime.toLocalTime().isAfter(businessHour.getOpenAt())
                                && currentTime.toLocalTime().isBefore(businessHour.getCloseAt()));
    }

    private SingleCurrentLocationStoreResponse mapToSingleCurrentLocationStoreResponse(StoreMeta storeMeta, LocalDateTime currentTime) {

        return SingleCurrentLocationStoreResponse.fromStoreMeta(isOpenStore(storeMeta, currentTime), storeMeta);
    }

    @Transactional
    public void createStore(CreateStoreRequest store) {

        StoreMeta storeMeta = saveStoreMeta(store);
        saveStoreDetail(store, storeMeta);
    }

    @Transactional
    public void deleteStoreMeta(long storeMetaId) {

        storeMetaRepository.findById(storeMetaId)
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 존재하지 않는 협업 지점 고유번호입니다."))
                .delete();
    }

    @Transactional(readOnly = true)
    public StoreMeta findStoreMetaById(long id) {

        return storeMetaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 존재하지 않는 협업 지점 고유번호입니다."));
    }

    private StoreMeta saveStoreMeta(CreateStoreRequest store) {

        Classification classification = classificationService.findClassificationById(store.getClassificationId());
        Classification subClassification = classificationService.findSubClassificationById(store.getSubClassificationId());

        List<SingleBusinessHourRequest> businessHourRequests = store.getBusinessHours();

        StoreMeta storeMeta = storeMetaRepository.save(StoreMeta.createStoreMetaForSave(store, classification, subClassification));

        List<BusinessHour> businessHours = businessHourRequests.stream()
                .map(businessHourRequest -> BusinessHour.ofCreateBusinessHour(businessHourRequest, storeMeta))
                .collect(Collectors.toUnmodifiableList());

        businessHourService.saveAllBusinessHour(businessHours);

        return storeMeta;
    }

    private void saveStoreDetail(CreateStoreRequest store, StoreMeta storeMeta) {

        storeDetailRepository.save(StoreDetail.createForSave(store, storeMeta));
    }
}
