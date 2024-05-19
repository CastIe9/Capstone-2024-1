package start.capstone2.controller.portfolio;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import start.capstone2.dto.ResponseResult;
import start.capstone2.dto.portfolio.PortfolioRequest;
import start.capstone2.dto.portfolio.PortfolioResponse;
import start.capstone2.service.portfolio.PortfolioService;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    @PostMapping("")
    public Long createPortfolio(Long userId) {
        return portfolioService.createPortfolio(userId);
    }


    @GetMapping("")
    public ResponseResult<List<PortfolioResponse>> findAllPortfolio(Long userId) {
        List<PortfolioResponse> results = portfolioService.findUserPortfolios(userId);
        return new ResponseResult<>(results);
    }

    @PutMapping("/{portfolioId}")
    public void updatePortfolio(Long userId, PortfolioRequest portfolioRequest, @PathVariable Long portfolioId) {
        portfolioService.updatePortfolio(userId, portfolioId, portfolioRequest);
    }

    @DeleteMapping("/{portfolioId}")
    public void deletePortfolio(Long userId, @PathVariable Long portfolioId) {
        portfolioService.deletePortfolio(userId, portfolioId);
    }

}
