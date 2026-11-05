package dio.budgeting.application;

import dio.budgeting.application.output.FinancialSummaryOutput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GetFinancialSummaryUseCase {

    private final TransactionRepository transactionRepository;

    public GetFinancialSummaryUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Tool(name = "getFinancialSummary", description = "Calcula e retorna um resumo financeiro completo com o total geral de gastos, total de transações e gastos agrupados por cada categoria")
    public FinancialSummaryOutput execute() {
        var transactions = transactionRepository.findAll();
        long totalAmount = transactions.stream().mapToLong(Transaction::getAmount).sum();
        int totalTransactions = transactions.size();

        Map<Category, Long> totalByCategory = transactions.stream()
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        () -> new EnumMap<>(Category.class),
                        Collectors.summingLong(Transaction::getAmount)
                ));

        return new FinancialSummaryOutput(totalAmount, totalTransactions, totalByCategory);
    }
}
