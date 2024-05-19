package start.capstone2.service.portfolio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import start.capstone2.domain.Image.Image;
import start.capstone2.domain.Image.ImageStore;
import start.capstone2.domain.portfolio.Portfolio;
import start.capstone2.domain.portfolio.PortfolioApi;
import start.capstone2.domain.portfolio.PortfolioDesign;
import start.capstone2.domain.portfolio.repository.PortfolioDesignRepository;
import start.capstone2.domain.portfolio.repository.PortfolioRepository;
import start.capstone2.domain.user.User;
import start.capstone2.domain.user.repository.UserRepository;
import start.capstone2.dto.portfolio.PortfolioApiResponse;
import start.capstone2.dto.portfolio.PortfolioCommentResponse;
import start.capstone2.dto.portfolio.PortfolioDesignRequest;
import start.capstone2.dto.portfolio.PortfolioDesignResponse;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PortfolioDesignService {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioDesignRepository designRepository;
    private final ImageStore imageStore;

    @Transactional
    public Long createPortfolioDesign(Long userId, Long portfolioId, PortfolioDesignRequest request) {
        User user = userRepository.findById(userId).orElseThrow();
        Portfolio portfolio = portfolioRepository.findById(portfolioId).orElseThrow();

        Image image = imageStore.saveImage(request.getImage());

        PortfolioDesign design = PortfolioDesign.createPortfolioDesign(
                portfolio,
                image,
                request.getExplain()
        );

        portfolio.addDesign(design);
        return design.getId();
    }

    @Transactional
    public void updatePortfolioDesign(Long userId, Long portfolioId, Long designId, PortfolioDesignRequest request) {
        PortfolioDesign design = designRepository.findByIdWithImage(portfolioId);

        // 이미지 삭제
        Image oldImage = design.getImage();
        ImageStore.removeImage(oldImage);

        Image newImage = imageStore.saveImage(request.getImage());
        design.updatePortfolioDesign(newImage, request.getExplain());
    }
    
    @Transactional
    public void deletePortfolioDesign(Long userId, Long portfolioId, Long designId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId).orElseThrow();
        PortfolioDesign design = designRepository.findById(designId).orElseThrow(); // 삭제하기 위해 2번 조회 -> 1번 조회하기 위해선 또 다른 식별자가 필요함
        portfolio.removeDesign(design);
    }

    public List<PortfolioDesignResponse> findPortfolioDesigns(Long userId, Long portfolioId) {
        List<PortfolioDesign> designs = designRepository.findAllByPortfolioId(portfolioId);
        List<PortfolioDesignResponse> results = new ArrayList<>();
        for (PortfolioDesign design : designs) {
            results.add(new PortfolioDesignResponse(design.getId(), design.getImage().getSavedName(), design.getExplain()));
        }
        return results;
    }

}
