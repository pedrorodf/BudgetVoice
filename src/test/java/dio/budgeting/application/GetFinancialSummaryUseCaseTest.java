package dio.budgeting.application;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetFinancialSummaryUseCaseTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private GetFinancialSummaryUseCase getFinancialSummaryUseCase;

    @Test
    @DisplayName("Deve calcular resumo financeiro corretamente com agrupamento por categoria")
    void shouldCalculateFinancialSummaryCorrectly() {
        var t1 = new Transaction("Supermercado", 10000L, Category.GROCERIES);
        var t2 = new Transaction("Padaria", 2000L, Category.GROCERIES);
        var t3 = new Transaction("Gasolina", 20000L, Category.AUTO);
        var t4 = new Transaction("Remédio", 5000L, Category.PHARMA);

        when(transactionRepository.findAll()).thenReturn(List.of(t1, t2, t3, t4));

        var summary = getFinancialSummaryUseCase.execute();

        assertThat(summary).isNotNull();
        assertThat(summary.totalAmount()).isEqualTo(37000L); // 10000 + 2000 + 20000 + 5000
        assertThat(summary.totalTransactions()).isEqualTo(4);

        assertThat(summary.totalByCategory()).containsEntry(Category.GROCERIES, 12000L);
        assertThat(summary.totalByCategory()).containsEntry(Category.AUTO, 20000L);
        assertThat(summary.totalByCategory()).containsEntry(Category.PHARMA, 5000L);

        verify(transactionRepository).findAll();
    }

    @Test
    @DisplayName("Deve retornar resumo zerado quando não houver transações")
    void shouldReturnZeroSummaryWhenNoTransactionsExist() {
        when(transactionRepository.findAll()).thenReturn(List.of());

        var summary = getFinancialSummaryUseCase.execute();

        assertThat(summary).isNotNull();
        assertThat(summary.totalAmount()).isZero();
        assertThat(summary.totalTransactions()).isZero();
        assertThat(summary.totalByCategory()).isEmpty();

        verify(transactionRepository).findAll();
    }
}
