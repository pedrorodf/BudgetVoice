package dio.budgeting.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionTest {

    @Test
    @DisplayName("Deve criar uma transação válida com ID gerado")
    void shouldCreateValidTransaction() {
        var transaction = new Transaction("Supermercado", 15000L, Category.GROCERIES);

        assertThat(transaction.getId()).isNotNull();
        assertThat(transaction.getDescription()).isEqualTo("Supermercado");
        assertThat(transaction.getAmount()).isEqualTo(15000L);
        assertThat(transaction.getCategory()).isEqualTo(Category.GROCERIES);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Deve lançar exceção quando a descrição for vazia ou em branco")
    void shouldThrowExceptionWhenDescriptionIsBlank(String invalidDescription) {
        assertThatThrownBy(() -> new Transaction(invalidDescription, 1000L, Category.GROCERIES))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("A descrição da transação não pode ser nula ou vazia.");
    }

    @Test
    @DisplayName("Deve lançar exceção quando a descrição for nula")
    void shouldThrowExceptionWhenDescriptionIsNull() {
        assertThatThrownBy(() -> new Transaction(null, 1000L, Category.GROCERIES))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("A descrição da transação não pode ser nula ou vazia.");
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, -5000L})
    @DisplayName("Deve lançar exceção quando o valor for zero ou negativo")
    void shouldThrowExceptionWhenAmountIsZeroOrNegative(long invalidAmount) {
        assertThatThrownBy(() -> new Transaction("Gasolina", invalidAmount, Category.AUTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("O valor da transação deve ser positivo.");
    }

    @Test
    @DisplayName("Deve lançar exceção quando a categoria for nula")
    void shouldThrowExceptionWhenCategoryIsNull() {
        assertThatThrownBy(() -> new Transaction("Remédio", 2500L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("A categoria da transação é obrigatória.");
    }
}
