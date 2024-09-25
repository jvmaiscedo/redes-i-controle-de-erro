/* ***************************************************************
 * Autor............: Joao Victor Gomes Macedo
 * Matricula........: 202210166
 * Inicio...........: 26/03/2024
 * Ultima alteracao.: 07/04/2024
 * Nome.............: CamadaDeAplicacaoTransmissora
 * Funcao...........: Agrupa os dados em um espaco menor de memoria
 *                    e encaminha para a Camada Fisica Transmissora.
 *************************************************************** */
package model;

import controller.MainController;

public class CamadaDeAplicacaoTransmissora {
  /* ***************************************************************
   * Metodo: gerarQuadro
   * Funcao: Gera um vetor de inteiros contendo os dados dos caracteres.
   * Parametros: String mensagem
   * Retorno: Sem retorno.
   *************************************************************** */
  public static void gerarQuadro(String mensagem) {
    char[] vetorMensagem = mensagem.toCharArray();
    int tamVet = (vetorMensagem.length / 4);
    if (vetorMensagem.length % 4 != 0) {
      tamVet += 1;
    }
    int[] fluxo = new int[tamVet];
    int bitPositiion = 7;
    int index = 0;
    for (int j = 0; j < vetorMensagem.length; j++) {
      if (j % 4 == 0 && j != 0) {
        index += 1;
        bitPositiion = 7;
      }
      fluxo[index] += armazenandoInfo(charParaBinario(vetorMensagem[j]), bitPositiion);
      bitPositiion += 8;
      MainController.bitsNaMensagem += 8;
    }
    CamadaEnlaceDadosTransmissora.CamadaEnlaceDadosTransmissora(fluxo);
  }

  /* ***************************************************************
   * Metodo: charParaBinario
   * Funcao: Gerar uma string com os bits do caractere.
   * Parametros: Ascii do caractere
   * Retorno: Array de char.
   *************************************************************** */
  public static char[] charParaBinario(int caractere) {
    String binario = "";
    for (int i = 7; i >= 0; i--) {
      int bit = (caractere >> i) & 1;
      binario += bit;
    }
    return binario.toCharArray();
  }

  /* ***************************************************************
   * Metodo: armazenandoInfo
   * Funcao: Insere num inteiro os bits de um caractere
   * Parametros: Vetor de char, inteiro indicando a posicao da mascara.
   * Retorno: Inteiro contendo o resultado
   *************************************************************** */
  public static int armazenandoInfo(char[] binario, int bitPosition) {
    int resultado = 0;
    for (int i = 0; i <= 7; i++) {
      if (binario[i] == '1') {
        int mask = 1 << bitPosition;
        resultado = resultado | mask;
      }
      bitPosition--;
    }
    return resultado;
  }

}
