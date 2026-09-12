package com.splitloom.optimizer.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OptimizedBasketResult {
    private StoreCartBreakdown singleStoreWinner;
    private List<StoreCartBreakdown> allSingleStoreOptions = new ArrayList<>();
    private List<StoreCartBreakdown> splitStoreRecommendation = new ArrayList<>();
    private boolean isSplitRecommended;
    private BigDecimal netSavingsInr = BigDecimal.ZERO;
    private String summaryMessage;

    public OptimizedBasketResult() {}

    public StoreCartBreakdown getSingleStoreWinner() { return singleStoreWinner; }
    public void setSingleStoreWinner(StoreCartBreakdown singleStoreWinner) { this.singleStoreWinner = singleStoreWinner; }

    public List<StoreCartBreakdown> getAllSingleStoreOptions() { return allSingleStoreOptions; }
    public void setAllSingleStoreOptions(List<StoreCartBreakdown> allSingleStoreOptions) { this.allSingleStoreOptions = allSingleStoreOptions; }

    public List<StoreCartBreakdown> getSplitStoreRecommendation() { return splitStoreRecommendation; }
    public void setSplitStoreRecommendation(List<StoreCartBreakdown> splitStoreRecommendation) { this.splitStoreRecommendation = splitStoreRecommendation; }

    public boolean isSplitRecommended() { return isSplitRecommended; }
    public void setSplitRecommended(boolean splitRecommended) { isSplitRecommended = splitRecommended; }

    public BigDecimal getNetSavingsInr() { return netSavingsInr; }
    public void setNetSavingsInr(BigDecimal netSavingsInr) { this.netSavingsInr = netSavingsInr; }

    public String getSummaryMessage() { return summaryMessage; }
    public void setSummaryMessage(String summaryMessage) { this.summaryMessage = summaryMessage; }
}
