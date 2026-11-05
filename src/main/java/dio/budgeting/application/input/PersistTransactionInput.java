package dio.budgeting.application.input;

import dio.budgeting.domain.Category;
import org.springframework.ai.tool.annotation.ToolParam;

public record PersistTransactionInput(@ToolParam(description = "Descrição da transação")String description,
                                      @ToolParam(description = "Valor da transação (em centavos)")long amount,
                                      @ToolParam(description = "Categoria da transação")Category category) {
}
