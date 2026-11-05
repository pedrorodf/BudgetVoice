package dio.budgeting.application.output;

import dio.budgeting.domain.Category;

import java.util.Map;

public record FinancialSummaryOutput(
        long totalAmount,
        int totalTransactions,
        Map<Category, Long> totalByCategory
) {}
