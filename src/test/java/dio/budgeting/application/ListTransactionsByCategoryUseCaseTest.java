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
class ListTransactionsByCategoryUseCaseTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private ListTransactionsByCategoryUseCase listTransactionsByCategoryUseCase;

    @Test
    @DisplayName("Deve retornar lista de transações filtradas por categoria")
    void shouldReturnTransactionsByCategory() {
        var t1 = new Transaction("Supermercado 1", 10000L, Category.GROCERIES);
        var t2 = new Transaction("Feira", 3500L, Category.GROCERIES);

        when(transactionRepository.findAllByCategory(Category.GROCERIES)).thenReturn(List.of(t1, t2));

        var result = listTransactionsByCategoryUseCase.execute(Category.GROCERIES);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).description()).isEqualTo("Supermercado 1");
        assertThat(result.get(0).category()).isEqualTo("GROCERIES");
        assertThat(result.get(1).description()).isEqualTo("Feira");

        verify(transactionRepository).findAllByCategory(Category.GROCERIES);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nenhuma transação for encontrada na categoria")
    void shouldReturnEmptyListWhenNoTransactionsFound() {
        when(transactionRepository.findAllByCategory(Category.AUTO)).thenReturn(List.of());

        var result = listTransactionsByCategoryUseCase.execute(Category.AUTO);

        assertThat(result).isEmpty();
        verify(transactionRepository).findAllByCategory(Category.AUTO);
    }
}
