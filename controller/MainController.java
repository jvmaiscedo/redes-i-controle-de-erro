/* ***************************************************************
* Autor............: Joao Victor Gomes Macedo
* Matricula........: 202210166
* Inicio...........: 26/03/2024
* Ultima alteracao.: 02/06/2024
* Nome.............: MainController
* Funcao...........: Manipula os objetos da interface gráfica 
		                 e das classes modelo.
*************************************************************** */
package controller;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Semaphore;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import model.AplicacaoReceptora;
import model.AplicacaoTransmissora;
import javafx.scene.image.ImageView;
import model.MeioDeComunicacao;


public class MainController implements Initializable {
  //Elementos FXML
  @FXML
  private ImageView SA1;
  @FXML
  private ImageView SA2;
  @FXML
  private ImageView SA3;
  @FXML
  private ImageView SA4;
  @FXML
  private ImageView SA5;
  @FXML
  private ImageView SA6;
  @FXML
  private ImageView SA7;
  @FXML
  private ImageView SA8;
  @FXML
  private ImageView SB1;
  @FXML
  private ImageView SB2;
  @FXML
  private ImageView SB3;
  @FXML
  private ImageView SB4;
  @FXML
  private ImageView SB5;
  @FXML
  private ImageView SB6;
  @FXML
  private ImageView SB7;
  @FXML
  private ImageView SB8;
  @FXML
  private ImageView T1;
  @FXML
  private ImageView T2;
  @FXML
  private ImageView T3;
  @FXML
  private ImageView T4;
  @FXML
  private ImageView T5;
  @FXML
  private ImageView T6;
  @FXML
  private ImageView T7;
  @FXML
  private TextArea textMessageTransmissor;
  @FXML
  private TextArea textMessageReceptor;
  @FXML
  private Button enviarMensagem;
  @FXML
  private Button limparSelecao;
  @FXML
  private Slider velocidadeFluxo;
  @FXML
  public TextArea bitsEnviados;
  @FXML
  private TextArea bitsRecebidos;
  @FXML
  private ChoiceBox<String> codificacao;
  @FXML
  private ChoiceBox<String> enquadramento;
  @FXML
  private ChoiceBox<String> controleErro;
  @FXML
  private ChoiceBox<String> taxaDeErro;
  @FXML
  private ChoiceBox<String> qtdBits;
  @FXML
  private ImageView erroNoMeio;
  @FXML
  private ImageView erro;

  //Elementos de controle
  private static int bitAnterior = 1;
  public static AplicacaoReceptora aplicacaoReceptora;
  public static MeioDeComunicacao meioDeComunicacao;
  private static int tipoDeCodificacao = 1;
  private static int tipoDeEnquadramento = 1;
  private static int tipoDeControleDeErro = 1;
  private static int taxaErro=0;
  private static int qtdDeBitsErro=1;
  static ImageView[] sinalAlto;
  static ImageView[] sinalBaixo;
  static ImageView[] transicao;
  public static Semaphore quadroEnviado;
  public static volatile boolean mensagemCompleta;
  public static volatile boolean erroDetectado;
  private static String [] algCodificacao = {"Binária", "Manchester","Manchester Diferencial"};
  private static String [] algEnquadramento = {"Contagem de Caracteres","Inserção de Bytes","Inserção de Bits","Violação de Camada Física"};
  private static String [] controleDeErro = {"Bit Paridade Par","Bit Paridade Ímpar", "CRC-32","Código de Hamming"};
  private static String [] porcentagemDeErro = {"0%", "10%", "20%","30%", "40%","50%", "60%","70%", "80%","90%", "100%"};
  private static String [] qtdBitsErro = {"1", "2","3"};
  public static String mensagemFinal;
  public static int bitsNaMensagem=0;
  public static int bitsNoQuadro=0;


  /* ***************************************************************
   * Metodo: initialize
   * Funcao: inicializar os elementos
   * Parametros: Padroes do JAVAFX
   * Retorno: Sem retorno
   *************************************************************** */
  @Override
  public void initialize(URL url, ResourceBundle rb) {
    sinalAlto = new ImageView[]{SA1, SA2, SA3, SA4, SA5, SA6, SA7, SA8};
    sinalBaixo = new ImageView[]{SB1, SB2, SB3, SB4, SB5, SB6, SB7, SB8};
    transicao = new ImageView[]{T1, T2, T3, T4, T5, T6, T7};
    aplicacaoReceptora = new AplicacaoReceptora(textMessageReceptor, erro);
    meioDeComunicacao = new MeioDeComunicacao(velocidadeFluxo, bitsEnviados, bitsRecebidos, erroNoMeio);
    enviarMensagem.setDisable(false);
    velocidadeFluxo.setValue(50);//metade do valor total.
    mensagemCompleta=false;
    codificacao.getItems().addAll(algCodificacao);
    codificacao.setValue("Binária");
    enquadramento.getItems().addAll(algEnquadramento);
    enquadramento.setValue("Contagem de Caracteres");
    controleErro.getItems().addAll(controleDeErro);
    controleErro.setValue("Bit Paridade Par");
    taxaDeErro.getItems().addAll(porcentagemDeErro);
    taxaDeErro.setValue("0%");
    qtdBits.getItems().addAll(qtdBitsErro);
    qtdBits.setValue("1");
    controleErro.setOnAction(this::selecionarControleDeErro);
    codificacao.setOnAction(this::selecionarCodificacao);
    enquadramento.setOnAction(this::selecionarEnquadramento);
    taxaDeErro.setOnAction(this::selecionarTaxaDeErro);
    qtdBits.setOnAction(this::selecionarQtdBitsDeErro);
    mensagemFinal = "";

  }

  /* ***************************************************************
   * Metodo: enviarMensagem
   * Funcao: dispara os eventos de envio de mensagem
   * Parametros: sem parametros
   * Retorno: Sem retorno.
   *************************************************************** */
  @FXML
  public void enviarMensagem() {
    mensagemCompleta=false;
    erroDetectado=false;
    MainController.bitsNoQuadro =0;
    erro.setVisible(false);
    erroNoMeio.setVisible(false);
    mensagemFinal = "";
    textMessageReceptor.setText("");
    limparSelecao.setDisable(true);
    quadroEnviado = new Semaphore(0);
    Alert alert = new Alert(Alert.AlertType.WARNING);
    if (textMessageTransmissor.getText().isEmpty()) {
      alert.setContentText("Mensagem vazia");
      alert.showAndWait();
    } else if (tipoDeCodificacao==0 || tipoDeEnquadramento==0 || tipoDeControleDeErro==0) {
      alert.setContentText("Escolha os serviços da camada");
      alert.showAndWait();
    } else if (tipoDeCodificacao==1 && tipoDeEnquadramento==4) {
      alert.setContentText("Codificação Binária e Violação de Camada\nsão serviços incompatíveis.");
      alert.showAndWait();
    } else {
      String texto = textMessageTransmissor.getText();
      AplicacaoTransmissora.enviarMensagem(texto);
    }
    limparSelecao.setDisable(false);

  }

  /* ***************************************************************
   * Metodo: limparSelecao
   * Funcao: limpa a tela e escolhas.
   * Parametros: sem parametros
   * Retorno: Sem retorno.
   *************************************************************** */
  @FXML
  public void limparSelecao() {
    textMessageTransmissor.setText("");
    textMessageReceptor.setText(null);
    bitsEnviados.setText("");
    bitsRecebidos.setText("");
    MainController.bitsNoQuadro=0;
    MainController.bitsNaMensagem=0;
  }
  public void selecionarControleDeErro(ActionEvent event){
    switch (controleErro.getValue()){
      case "Bit Paridade Par":
        tipoDeControleDeErro = 1;
        break;
      case  "Bit Paridade Ímpar":
        tipoDeControleDeErro=2;
        break;
      case "CRC-32":
        tipoDeControleDeErro=3;
        break;
      case "Código de Hamming":
        tipoDeControleDeErro=4;
        break;
    }
  }
  /* ***************************************************************
   * Metodo: selecionarCodificacao
   * Funcao: Escolher o tipo de codificacao com base na choicebox
   * Parametros: Evento de clique
   * Retorno: Sem retorno.
   *************************************************************** */
  public void selecionarCodificacao(ActionEvent event) {
    switch (codificacao.getValue()) {
      case "Binária":
        tipoDeCodificacao = 1;
        break;
      case "Manchester":
        tipoDeCodificacao = 2;
        break;
      case "Manchester Diferencial":
        tipoDeCodificacao = 3;
        break;
    }
  }


  public void selecionarEnquadramento(ActionEvent event) {
    switch (enquadramento.getValue()) {
      case "Contagem de Caracteres":
        tipoDeEnquadramento = 1;
        break;
      case "Inserção de Bytes":
        tipoDeEnquadramento = 2;
        break;
      case "Inserção de Bits":
        tipoDeEnquadramento = 3;
        break;
      case "Violação de Camada Física":
        tipoDeEnquadramento = 4;
        break;
    }
  }
  public void selecionarTaxaDeErro(ActionEvent event) {
    switch (taxaDeErro.getValue()) {
      case "0%":
        taxaErro = 0;
        break;
      case "10%":
        taxaErro = 1;
        break;
      case "20%":
        taxaErro = 2;
        break;
      case "30%":
        taxaErro = 3;
        break;
      case "40%":
        taxaErro = 4;
        break;
      case "50%":
        taxaErro = 5;
        break;
      case "60%":
        taxaErro = 6;
        break;
      case "70%":
        taxaErro = 7;
        break;
      case "80%":
        taxaErro = 8;
        break;
      case "90%":
        taxaErro = 9;
        break;
      case "100%":
        taxaErro = 10;
        break;
      default:
        throw new IllegalArgumentException("Taxa de erro desconhecida: " + taxaDeErro.getValue());
    }
  }
  public void selecionarQtdBitsDeErro(ActionEvent event){
    switch (qtdBits.getValue()){
      case "1":
        qtdDeBitsErro=1;
        break;
      case "2":
        qtdDeBitsErro=2;
        break;
      case "3":
        qtdDeBitsErro=3;
        break;
    }
  }

  /* ***************************************************************
   * Metodo: atualizarPrimeiroSinal
   * Funcao: atualiza o primeiro sinal na tela.
   * Parametros: inteiro bit e inteiro bit de controle.
   * Retorno: Sem retorno.
   *************************************************************** */
  public static void atualizaPrimeiroSinal(int bit, int bitInicial) {
    Platform.runLater(() -> {
      sinalAlto[0].setVisible(false);
      sinalBaixo[0].setVisible(false);
      transicao[0].setVisible(false);
      if (bit == 0) {
        sinalBaixo[0].setVisible(true);
      }
      if (bit == 1) {
        sinalAlto[0].setVisible(true);
      }
      if (bit == -1) {
        sinalAlto[0].setVisible(false);
        sinalBaixo[0].setVisible(false);
      }
      if (bitAnterior != bit && bitInicial != 0 && bit != -1) {
        transicao[0].setVisible(true);
      } else transicao[0].setVisible(false);
      bitAnterior = bit;
    });
  }
  /* ***************************************************************
   * Metodo: atualizarOnda
   * Funcao: atualiza visibilidade das imagens gerando sensacao de
   *         movimento
   * Parametros: sem parametros
   * Retorno: Sem retorno.
   *************************************************************** */
  public static void atualizarOnda() {
    Platform.runLater(() -> {
      for (int i = 7; i > 0; i--) {
        sinalAlto[i].setVisible(sinalAlto[i - 1].isVisible());
        sinalBaixo[i].setVisible(sinalBaixo[i - 1].isVisible());
        if (i != 7) {
          transicao[i].setVisible(transicao[i - 1].isVisible());
        }
      }
    });
  }
  /* ***************************************************************
   * Metodo: textArea
   * Funcao: alterar o conteudo do TextArea com base no bits.
   * Parametros: Quadro com bits.
   * Retorno: String informando os bits.
   *************************************************************** */
  static public String textArea(int fim,int[] quadro) {
    StringBuilder sequencia = new StringBuilder();
    int index = 0;
    int desloc = 0;
    for (int i = 0; i < fim; i++) {
      if (desloc == 32) {
        index++;
        desloc = 0;
      }
      int bit = (quadro[index] & 1 << desloc) >> desloc;
      if (bit == -1) {
        bit = 1;
      }
      desloc++;
      sequencia.append(bit);
    }
    return sequencia.reverse().toString();
  }

  public static int getTipoDeCodificacao() {
    return tipoDeCodificacao;
  }

  public static int getTipoDeEnquadramento() {
    return tipoDeEnquadramento;
  }

  public static int getTipoDeControleDeErro() {
    return tipoDeControleDeErro;
  }
  public static int getTaxaErro(){
    return taxaErro;
  }
  public static int getQtdDeBitsErro(){
    return qtdDeBitsErro;
  }
}

