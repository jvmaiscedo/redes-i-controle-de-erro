/* ***************************************************************
 * Autor............: Joao Victor Gomes Macedo
 * Matricula........: 202210166
 * Inicio...........: 26/03/2024
 * Ultima alteracao.: 07/04/2024
 * Nome.............: CamadaFisicaReceptora
 * Funcao...........: Decodifica o fluxo de bits recebidos e
 *                    encaminha a camada de enlace de dados receptora.
 *************************************************************** */
package model;

import controller.MainController;
import javafx.application.Platform;
import javafx.scene.control.Alert;

public class CamadaFisicaReceptora {

  /* ***************************************************************
   * Metodo: decodificaQuadro
   * Funcao: Decodifica o fluxo de bits de acordo a codificacao escolhida
   * Parametros: Vetor de inteiros
   * Retorno: Sem retorno.
   *************************************************************** */
  public static void decodificaQuadro(int[] fluxoDeBits) {
    int[] quadro;
    if (MainController.getTipoDeEnquadramento() == 4) {
      int[] quadroCE;
      quadroCE = RecepetoraControleDeErroCVF(fluxoDeBits);
      if (quadroCE == null) {
        MainController.aplicacaoReceptora.exibeMensagem("ERRO");
        return;
      } else {
        fluxoDeBits = violacaoCamadaFisicaReceptora(quadroCE);
      }
    }
    switch (MainController.getTipoDeCodificacao()) {
      case 1:
        quadro = fluxoDeBits;
        break;
      case 2:
        quadro = decodificaManchester(fluxoDeBits);
        break;
      case 3:
        quadro = decodificaManchesterDiferencial(fluxoDeBits);
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + MainController.getTipoDeCodificacao());
    }
    CamadaEnlaceDadosReceptora.CamadaDeEnlaceTransmissoraControleDeErro(quadro);

  }

  /* ***************************************************************
   * Metodo: decodificaManchester
   * Funcao: Decodificar as informacoes enviadas por meio da codificacao
   *         Manchester
   * Parametros: vetor de inteiros
   * Retorno: Vetor de inteiros contendo o quadro de bits decodificado
   *************************************************************** */
  public static int[] decodificaManchester(int[] fluxoDeBits) {
    int tam;
    if (fluxoDeBits.length % 2 == 0) {
      tam = fluxoDeBits.length / 2;
    } else {
      tam = (fluxoDeBits.length / 2) + 1;
    }
    int[] quadro = new int[tam];
    int indexManch = 0;
    int bitPositionManch = 1;
    int indexQuadro = 0;
    int bitPosition = 0;
    int fim = MainController.bitsNoQuadro / 2;
    for (int i = 0; i < fim; i++) {

      if (bitPosition == 32) {
        indexQuadro++;
        bitPosition = 0;
      }
      if (bitPositionManch > 32) {
        indexManch++;
        bitPositionManch = 1;
      }
      int bitInfo1 = Math.abs((fluxoDeBits[indexManch] >> bitPositionManch) & 1);
      int bitInfo2 = Math.abs((fluxoDeBits[indexManch] >> bitPositionManch - 1) & 1);
      if (bitInfo1 != 0 && bitInfo2 == 0) {
        quadro[indexQuadro] |= (1 << bitPosition);
      }
      if (bitInfo1 == 1 && bitInfo2 == 0) {
        quadro[indexQuadro] |= (1 << bitPosition);
      } else if (bitInfo1 == 0 && bitInfo2 == 1) {
        quadro[indexQuadro] |= (0 << bitPosition);
      } else {
        Platform.runLater(() -> {
          Alert alert = new Alert(Alert.AlertType.ERROR);
          alert.setTitle("ERRO MANCHESTER");
          alert.setContentText("A sequencia: " + bitInfo1 + bitInfo2 + "\nnao sao reconhecidas");
          alert.showAndWait();
        });
        try {
          Thread.sleep(2);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
      bitPosition++;
      bitPositionManch += 2;
    }
    MainController.bitsNoQuadro /= 2;
    return quadro;
  }

  /* ***************************************************************
   * Metodo: decodificaManchesterDiferencial
   * Funcao: Decodificar as informacoes enviadas por meio da codificacao
   *         Manchester Diferencial
   * Parametros: vetor de inteiros
   * Retorno: Vetor de inteiros contendo o quadro de bits decodificado
   *************************************************************** */
  public static int[] decodificaManchesterDiferencial(int[] fluxoDeBits) {
    int[] quadro;
    int tam;
    if (fluxoDeBits.length % 2 == 0) {
      tam = fluxoDeBits.length / 2;
    } else {
      tam = (fluxoDeBits.length / 2) + 1;
    }
    quadro = new int[tam];
    int indexFluxoDeBits = 0;
    int indexQuadro = 0;
    int bitPositionManchDif = 0;
    int bitPosition = 0;
    int verificaAnterior = 1;
    for (int i = 0; i < MainController.bitsNoQuadro / 2; i++) {//tam se refere ao tamanho da mensagem, pois, como o bit 0 eh codificado em 01, caso eu tenha um for maior que o tamanho da mensagem, estarei codificando todos os bits 0 de posicoes vazias com 01.
      if (bitPosition == 32) {
        indexQuadro++;
        bitPosition = 0;
      }
      if (bitPositionManchDif == 32) {
        indexFluxoDeBits++;//aumentando o indice, pois ja guardou info de dois caracteres em um int.
        bitPositionManchDif = 0;//retomando a posicao para setar a mascara corretamente e guardar info de 1 caracter em 16 bits.
      }
      int bitInfo = (fluxoDeBits[indexFluxoDeBits] & (1 << bitPositionManchDif)) >> bitPositionManchDif;
      bitInfo = Math.abs(bitInfo);
      if (bitInfo == verificaAnterior) {
        quadro[indexQuadro] |= 1 << bitPosition;//colocando o valor 1 na posicao devida
        verificaAnterior = (fluxoDeBits[indexFluxoDeBits] & (1 << (bitPositionManchDif + 1))) >> (bitPositionManchDif + 1);
        verificaAnterior = Math.abs(verificaAnterior);

      } else {
        quadro[indexQuadro] |= (0) << bitPosition;
        verificaAnterior = (fluxoDeBits[indexFluxoDeBits] & (1 << (bitPositionManchDif + 1))) >> (bitPositionManchDif + 1);
        verificaAnterior = Math.abs(verificaAnterior);

      }
      bitPosition++;
      bitPositionManchDif += 2;
    }
    MainController.bitsNoQuadro /= 2;
    return quadro;
  }

  /* ***************************************************************
   * Metodo: violacaoCamadaFisicaReceptora
   * Funcao: Desenquadrar quadro enquadrado por violacao de camada
   *         fisica.
   * Parametros: vetor de inteiros
   * Retorno: Vetor de inteiros contendo o quadro de bits decodificado
   *************************************************************** */
  private static int[] violacaoCamadaFisicaReceptora(int[] quadroEnquadrado) {
    int[] quadro;
    int deslocQuadroEnquadrado = 0;
    int deslocQuadro = 0;
    int indiceQuadroEnquadrado = 0;
    int indiceQuadro = 0;
    int bitsNoQuadroEnquadrado = MainController.bitsNoQuadro;
    int qtdCaracteresEnquadrados;
    if (bitsNoQuadroEnquadrado < 38) {
      qtdCaracteresEnquadrados = 1;
    } else {
      if (bitsNoQuadroEnquadrado % 38 == 0) {
        qtdCaracteresEnquadrados = (bitsNoQuadroEnquadrado / 38) * 2;
      } else {
        qtdCaracteresEnquadrados = ((bitsNoQuadroEnquadrado / 38) * 2) + 1;
      }
    }

    int tam = qtdCaracteresEnquadrados / 2;
    if (qtdCaracteresEnquadrados % 2 != 0) {
      tam += 1;
    }
    quadro = new int[tam];
    for (int i = 0; i < qtdCaracteresEnquadrados; i++) {//
      if (deslocQuadro == 32) {
        indiceQuadro++;
        deslocQuadro = 0;
      }
      if (i % 2 == 0) {
        if (i == 0) {
          for (int j = 0; j < 3; j++) {
            deslocQuadroEnquadrado++;
          }
        } else {
          for (int j = 0; j < 6; j++) {
            if (deslocQuadroEnquadrado == 32) {
              indiceQuadroEnquadrado++;
              deslocQuadroEnquadrado = 0;
            }
            deslocQuadroEnquadrado++;
          }
        }
      }
      for (int j = 0; j < 16; j++) {
        if (deslocQuadroEnquadrado == 32) {
          indiceQuadroEnquadrado++;
          deslocQuadroEnquadrado = 0;
        }
        int bit = (quadroEnquadrado[indiceQuadroEnquadrado] & 1 << deslocQuadroEnquadrado) >> deslocQuadroEnquadrado;
        if (bit == -1) {
          bit = 1;
        }
        quadro[indiceQuadro] |= bit << deslocQuadro;
        deslocQuadroEnquadrado++;
        deslocQuadro++;
      }
    }
    MainController.bitsNoQuadro -= 6;
    return quadro;
  }

  /* ***************************************************************
   * Metodo: RecepetoraControleDeErroCVF
   * Funcao: Verifica se houve erro no quadro recebido com base no
   *         algoritmo de deteccao de erro escolhido na interface
   * Parametros: vetor de inteiros
   * Retorno: Vetor de inteiros sem o controle de erro.
   **************************************************************** */
  static public int[] RecepetoraControleDeErroCVF(int quadro[]) {
    int[] quadroVerificado;
    int algControleDeErro = MainController.getTipoDeControleDeErro();
    switch (algControleDeErro) {
      case 1:
        quadroVerificado = ReceptoraControleDeErroBitParidadeParVCF(quadro);
        break;
      case 2:
        quadroVerificado = ReceptoraControleDeErroBitParidadeImparVCF(quadro);
        break;
      case 3:
        quadroVerificado = ReceptoraControleDeErroCRCVCF(quadro);
        break;
      case 4:
        quadroVerificado = ReceptoraControleDeErroCodigoHammingVCF(quadro);
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + algControleDeErro);
    }
    return quadroVerificado;
  }

  /* ***************************************************************
   * Metodo: ReceptoraControleDeErroBitParidadeParVCF
   * Funcao: Verifica se houve erro no quadro usando o algoritmo de
   *         Bit Paridade Par.
   * Parametros: vetor de inteiros
   * Retorno: quadro sem bits de controle de erro ou null se o erro
   *          foi detectado.
   **************************************************************** */
  static int[] ReceptoraControleDeErroBitParidadeParVCF(int[] quadro) {
    int count = 0;
    int index = 0;
    int desloc = 0;
    int posicaoBitControle = (MainController.bitsNoQuadro % 32) - 1;
    for (int i = 0; i < MainController.bitsNoQuadro; i++) {
      if (i % 32 == 0 && i != 0) {
        index++;
        desloc = 0;
      }
      int bit = (quadro[index] & 1 << desloc) >> desloc;
      if (bit == -1) {
        bit = 1;
      }
      if (bit == 1) {
        count++;
      }
      desloc++;
    }
    if (count % 2 == 0) {
      quadro[quadro.length - 1] |= ((0) << posicaoBitControle);
      MainController.bitsNoQuadro--;
      return quadro;
    } else {
      return null;
    }
  }

  /* ***************************************************************
   * Metodo: ReceptoraControleDeErroBitParidadeImparVCF
   * Funcao: Verifica se houve erro no quadro usando o algoritmo de
   *         Bit Paridade Impar.
   * Parametros: vetor de inteiros
   * Retorno: quadro sem bits de controle de erro ou null se o erro
   *          foi detectado.
   **************************************************************** */
  static int[] ReceptoraControleDeErroBitParidadeImparVCF(int[] quadro) {
    int count = 0;
    int index = 0;
    int desloc = 0;
    int posicaoBitControle = (MainController.bitsNoQuadro % 32) - 1;
    for (int i = 0; i < MainController.bitsNoQuadro; i++) {
      if (i % 32 == 0 && i != 0) {
        index++;
        desloc = 0;
      }
      int bit = (quadro[index] & 1 << desloc) >> desloc;
      if (bit == -1) {
        bit = 1;
      }
      if (bit == 1) {
        count++;
      }
      desloc++;
    }
    if (count % 2 != 0) {
      quadro[quadro.length - 1] |= ((0) << posicaoBitControle);
      MainController.bitsNoQuadro--;
      return quadro;
    } else {
      return null;
    }
  }

  /* ***************************************************************
   * Metodo: ReceptoraControleDeErroCRCVCF
   * Funcao: Verifica se houve erro no quadro usando o algoritmo de
   *         CRC.
   * Parametros: vetor de inteiros
   * Retorno: quadro sem bits de controle de erro ou null se o erro
   *          foi detectado.
   **************************************************************** */
  private static int[] ReceptoraControleDeErroCRCVCF(int[] quadroControleErro) {
    int[] quadro = new int[quadroControleErro.length - 1];
    StringBuilder crc32Polinomio = new StringBuilder("100000100110000010001110110110111");
    StringBuilder mensagem = new StringBuilder();
    int fim = MainController.bitsNoQuadro;
    int desloc = 0;
    int index = 0;
    for (int i = 0; i < fim; i++) {//extraindo os bits do quadro e colocando em uma string para verificacao do resto
      if (i % 32 == 0 && i != 0) {
        index++;
        desloc = 0;
      }
      int bit = (quadroControleErro[index] & 1 << desloc) >> desloc;
      mensagem.append(Math.abs(bit));
      desloc++;
    }
    mensagem.reverse();
    if (calcularCRC32(mensagem.toString(), crc32Polinomio.toString()) != 0) {//se o resto calculado for diferente de 0, houve erro
      return null;
    } else {//caso nao tenha erro, coloca-se os bits em um vetor de inteiros
      StringBuilder mensagemSemCE = new StringBuilder(mensagem.toString().substring(0, mensagem.length() - crc32Polinomio.length() + 1));
      index = 0;
      desloc = 0;
      for (int i = mensagemSemCE.length() - 1; i >= 0; i--) {
        if (desloc == 32) {
          index++;
          desloc = 0;
        }
        int bit = mensagemSemCE.charAt(i) - '0'; //
        quadro[index] |= (bit << desloc);
        desloc++;
      }
      MainController.bitsNoQuadro -= 32;
      return quadro;
    }
  }


  public static long calcularCRC32(String msg, String crc) {
    int n = crc.length();
    StringBuilder encoded = new StringBuilder(msg);

    // Realiza a operação de divisão polinomial usando XOR
    for (int i = 0; i <= encoded.length() - n; ) {
      for (int j = 0; j < n; j++) {
        encoded.setCharAt(i + j, encoded.charAt(i + j) == crc.charAt(j) ? '0' : '1');
      }
      while (i < encoded.length() && encoded.charAt(i) != '1') {
        i++;
      }
    }
    long resto = Integer.parseUnsignedInt(encoded.substring(encoded.length() - n), 2);
    return resto;

  }

  /* ***************************************************************
   * Metodo: ReceptoraControleDeErroCodigoHammingVCF
   * Funcao: Verifica se houve erro no quadro usando o algoritmo de
   *         Codigo de Hamming.
   * Parametros: vetor de inteiros
   * Retorno: quadro sem bits de controle de erro ou null se o erro
   *          foi detectado.
   **************************************************************** */
  public static int[] ReceptoraControleDeErroCodigoHammingVCF(int[] quadro) {
    StringBuilder mensagemCodificada = new StringBuilder();
    int fim = MainController.bitsNoQuadro;
    int desloc = 0;
    int index = 0;
    for (int i = 0; i < fim; i++) {//colocando os bits em uma string para verificar a paridade dos bits de controle
      if (i % 32 == 0 && i != 0) {
        index++;
        desloc = 0;
      }
      int bit = (quadro[index] & (1 << desloc)) >> desloc;
      bit = Math.abs(bit);
      mensagemCodificada.append(bit);
      desloc++;
    }
    mensagemCodificada.reverse();
    int totalBits = mensagemCodificada.length();
    int r = 0;//quantidade de bits de controle
    while (Math.pow(2, r) < (totalBits + 1)) {//determinando quantos bits de controle sera necessario dado o tamanho da mensagem.
      r++;
    }
    char[] codigoHamming = mensagemCodificada.toString().toCharArray();//traansformando a mensagem codificada em um vetor de char para calcular a paridade.
    boolean erro = false;
    for (int i = 0; i < r; i++) {//verificando a paridade
      int posicaoBitDeControle = (int) Math.pow(2, i) - 1;
      boolean paridade = CalcularBitParidade(codigoHamming, posicaoBitDeControle);
      if (!paridade) {
        erro = true;
        break;
      }
    }
    if (erro) {
      return null;
    }
    StringBuilder mensagemSemParidade = new StringBuilder();//caso nao haja erro, extrair da mensagem somente os bits de dados.
    for (int i = 1; i <= totalBits; i++) {
      if ((i & (i - 1)) != 0) {
        mensagemSemParidade.append(codigoHamming[i - 1]);
      }
    }
    int m = mensagemSemParidade.length();
    int tam = m % 32 == 0 ? m / 32 : (m / 32 + 1);
    int[] quadroDecodificado = new int[tam];
    index = 0;
    desloc = 0;
    for (int i = mensagemSemParidade.length() - 1; i >= 0; i--) {//colocando os dados da mensagem em um vetor de inteiros.
      if (desloc == 32) {
        index++;
        desloc = 0;
      }
      int bit = mensagemSemParidade.charAt(i) - '0'; //
      quadroDecodificado[index] |= (bit << desloc);
      desloc++;
    }
    MainController.bitsNoQuadro = mensagemSemParidade.length();
    return quadroDecodificado;
  }

  /* ***************************************************************
   * Metodo: CalcularBitParidade
   * Funcao: Calcular a paridade do bit de controle
   * Parametros: Vetor com codigo de hamming e posicao do bit de controle.
   * Retorno: Booleana se a paridade esta correta ou ao.
   *************************************************************** */
  private static boolean CalcularBitParidade(char[] codigoHamming, int posicaoBit) {
    int count = 0;
    posicaoBit++;
    // Verificar a paridade dos bits cobertos pelo bitParidade(incluso) na posicaoBit
    for (int i = posicaoBit; i <= codigoHamming.length; i++) {
      if (((i >> (Integer.numberOfTrailingZeros(posicaoBit))) & 1) == 1) {
        if (codigoHamming[i - 1] == '1') {
          count++;
        }
      }
    }
    return count % 2 == 0;
  }
}
