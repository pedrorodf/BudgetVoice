package dio.budgeting.infrastructure.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import dio.budgeting.application.GetFinancialSummaryUseCase;
import dio.budgeting.application.ListTransactionsByCategoryUseCase;
import dio.budgeting.application.PersistTransactionUseCase;
import dio.budgeting.application.output.FinancialSummaryOutput;
import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.Category;
import dio.budgeting.infrastructure.http.exception.GlobalExceptionHandler;
import dio.budgeting.infrastructure.http.request.TransactionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private PersistTransactionUseCase persistTransactionUseCase;

    @Mock
    private ListTransactionsByCategoryUseCase listTransactionsByCategoryUseCase;

    @Mock
    private GetFinancialSummaryUseCase getFinancialSummaryUseCase;

    @Mock
    private TranscriptionModel transcriptionModel;

    @Mock
    private TextToSpeechModel textToSpeechModel;

    @Mock
    private Resource systemPrompt;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient.Builder chatClientBuilder;

    @BeforeEach
    void setUp() throws IOException {
        objectMapper = new ObjectMapper();

        when(systemPrompt.getContentAsString(any(Charset.class))).thenReturn("Prompt do sistema");
        when(chatClientBuilder.defaultSystem(any()).defaultTools(any(), any(), any()).build())
                .thenReturn(mock(ChatClient.class));

        var controller = new TransactionController(
                persistTransactionUseCase,
                listTransactionsByCategoryUseCase,
                getFinancialSummaryUseCase,
                transcriptionModel,
                systemPrompt,
                chatClientBuilder,
                textToSpeechModel
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /transactions - Deve criar transação com sucesso (HTTP 201)")
    void shouldCreateTransactionSuccessfully() throws Exception {
        var request = new TransactionRequest("Almoço", Category.GROCERIES, 4500L);
        var output = new TransactionOutput("123-uuid", "Almoço", "GROCERIES", 45.0);

        when(persistTransactionUseCase.execute(any())).thenReturn(output);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("123-uuid"))
                .andExpect(jsonPath("$.description").value("Almoço"))
                .andExpect(jsonPath("$.amount").value(45.0))
                .andExpect(jsonPath("$.category").value("GROCERIES"));

        verify(persistTransactionUseCase).execute(any());
    }

    @Test
    @DisplayName("POST /transactions - Deve falhar com 400 quando descrição for em branco")
    void shouldReturn400WhenDescriptionIsBlank() throws Exception {
        var jsonBody = """
                {
                    "description": "",
                    "category": "GROCERIES",
                    "amount": 1000
                }
                """;

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fields.description").exists());
    }

    @Test
    @DisplayName("POST /transactions - Deve falhar com 400 quando o valor for zero ou negativo")
    void shouldReturn400WhenAmountIsZeroOrNegative() throws Exception {
        var jsonBody = """
                {
                    "description": "Mercado",
                    "category": "GROCERIES",
                    "amount": -50
                }
                """;

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fields.amount").exists());
    }

    @Test
    @DisplayName("POST /transactions - Deve falhar com 400 quando categoria for nula")
    void shouldReturn400WhenCategoryIsNull() throws Exception {
        var jsonBody = """
                {
                    "description": "Mercado",
                    "amount": 1000
                }
                """;

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fields.category").exists());
    }

    @Test
    @DisplayName("GET /transactions/{category} - Deve listar transações por categoria (HTTP 200)")
    void shouldListTransactionsByCategory() throws Exception {
        var output1 = new TransactionOutput("id-1", "Mercado", "GROCERIES", 100.0);
        var output2 = new TransactionOutput("id-2", "Feira", "GROCERIES", 30.0);

        when(listTransactionsByCategoryUseCase.execute(Category.GROCERIES))
                .thenReturn(List.of(output1, output2));

        mockMvc.perform(get("/transactions/GROCERIES"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].description").value("Mercado"))
                .andExpect(jsonPath("$[1].description").value("Feira"));

        verify(listTransactionsByCategoryUseCase).execute(Category.GROCERIES);
    }

    @Test
    @DisplayName("GET /transactions/summary - Deve retornar o resumo financeiro (HTTP 200)")
    void shouldReturnFinancialSummary() throws Exception {
        var summary = new FinancialSummaryOutput(
                25000L,
                3,
                Map.of(Category.GROCERIES, 15000L, Category.AUTO, 10000L)
        );

        when(getFinancialSummaryUseCase.execute()).thenReturn(summary);

        mockMvc.perform(get("/transactions/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(25000))
                .andExpect(jsonPath("$.totalTransactions").value(3))
                .andExpect(jsonPath("$.totalByCategory.GROCERIES").value(15000))
                .andExpect(jsonPath("$.totalByCategory.AUTO").value(10000));

        verify(getFinancialSummaryUseCase).execute();
    }
}
