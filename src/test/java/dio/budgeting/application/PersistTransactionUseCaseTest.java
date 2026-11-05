package dio.budgeting.application;

import dio.budgeting.application.input.PersistTransactionInput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersistTransactionUseCaseTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private PersistTransactionUseCase persistTransactionUseCase;

    @Test
    @DisplayName("Deve persistir a transação e retornar o output correspondente")
    void shouldPersistTransactionSuccessfully() {
        var input = new PersistTransactionInput("Farmácia", 5500L, Category.PHARMA);
        var savedTransaction = new Transaction("Farmácia", 5500L, Category.PHARMA);

        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        var output = persistTransactionUseCase.execute(input);

        assertThat(output).isNotNull();
        assertThat(output.description()).isEqualTo("Farmácia");
        assertThat(output.value()).isEqualTo(5500.0);
        assertThat(output.category()).isEqualTo("PHARMA");

        var captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        var captured = captor.getValue();
        assertThat(captured.getDescription()).isEqualTo("Farmácia");
        assertThat(captured.getAmount()).isEqualTo(5500L);
        assertThat(captured.getCategory()).isEqualTo(Category.PHARMA);
    }
}
