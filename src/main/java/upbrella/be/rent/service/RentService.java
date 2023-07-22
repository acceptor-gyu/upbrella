package upbrella.be.rent.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upbrella.be.rent.dto.request.RentUmbrellaByUserRequest;
import upbrella.be.rent.repository.RentRepository;
import upbrella.be.store.entity.StoreMeta;
import upbrella.be.store.service.StoreMetaService;
import upbrella.be.umbrella.entity.Umbrella;
import upbrella.be.umbrella.service.UmbrellaService;
import upbrella.be.user.entity.User;
import upbrella.be.user.service.UserService;

@Service
@RequiredArgsConstructor
public class RentService {

    private final UmbrellaService umbrellaService;
    private final UserService userService;
    private final StoreMetaService storeMetaService;

    @Transactional
    public void addRental(RentUmbrellaByUserRequest rentUmbrellaByUserRequest) {

        Umbrella willRentUmbrella = umbrellaService.findById(rentUmbrellaByUserRequest.getUmbrellaId());

        // TODO: 추후에 세션으로 검증된 유저로 변경
        // 임시로 mock user 사용
        User userToRent = userService.findById(1L);

        StoreMeta rentalStore = storeMetaService.findById(rentUmbrellaByUserRequest.getStoreId());




    }
}
