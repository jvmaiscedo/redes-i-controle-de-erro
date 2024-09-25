/* ***************************************************************
 * Autor............: Joao Victor Gomes Macedo
 * Matricula........: 202210166
 * Inicio...........: 26/03/2024
 * Ultima alteracao.: 07/04/2024
 * Nome.............: CamadaDeAplicacaoReceptora
 * Funcao...........: Recebe o quadro decodificado transforma em
 *                    mensagem e envia a aplicacao receptora.
 *************************************************************** */
package model;

import controller.MainController;

public class CamadaDeAplicacaoReceptora {

  /* ***************************************************************
   * Metodo: decode
   * Funcao: Agrupar os dados contidos no quadro em uma mensagem.
   * Parametros: vetor de inteiros
   * Retorno: Sem retorno.
   *************************************************************** */
  public static void decode(int[] quadro) {
    String mensagem = "";
    for (int k : quadro) {
      mensagem += decodeInfo(k);
    }
    MainController.aplicacaoReceptora.exibeMensagem(mensagem);
  }

  /* ***************************************************************
   * Metodo: decodeInfo
   * Funcao: Decodifica os caracteres contidos em um inteiro
   * Parametros: inteiro contendo os caracteres
   * Retorno: string formada pelos caracteres.
   *************************************************************** */
  public static String decodeInfo(int info) {
    StringBuilder resultado = new StringBuilder();
    int asciiNumber = 0;
    int bitLigado = 7;
    for (int i = 4; i >= 1; i--) {
      int position = i * 8;
      for (int j = position - 1; j >= position - 8; j--) {
        int bitInfo = (info >> j) & 1;
        if (bitInfo == 1) {
          asciiNumber += Math.pow(2, bitLigado);//encontrando o valor ascii do caractere.
        }
        bitLigado--;
      }
      if (asciiNumber != 0) {//adicionando o caractere caso ele nao seja null
        resultado.append((char) asciiNumber);
      }
      bitLigado = 7;//resetando bitLigado para o proximo conjunto de 8 bits.
      asciiNumber = 0;//setando para 0 para ler o proximo conjunto de 8 bits.
    }
    return resultado.reverse().toString();
  }
}
