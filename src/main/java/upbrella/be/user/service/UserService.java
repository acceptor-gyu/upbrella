package upbrella.be.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import upbrella.be.user.dto.request.JoinRequest;
import upbrella.be.user.exception.ExistingMemberException;
import upbrella.be.user.entity.User;
import upbrella.be.user.repository.UserRepository;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public long login(Long socialId) {

        User foundUser = userRepository.findBySocialId(socialId)
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 존재하지 않는 회원입니다. 회원 가입을 해주세요."));

        return foundUser.getId();
    }

    public long join(long socialId, JoinRequest joinRequest) {

        if (userRepository.existsBySocialId(socialId)) {
            throw new ExistingMemberException("[ERROR] 이미 가입된 회원입니다. 로그인 폼으로 이동합니다.");
        }

        User joinedUser = userRepository.save(User.createNewUser(socialId, joinRequest));

        return joinedUser.getId();
    }
}
