/* ***************************************************************
 * Autor............: Joao Victor Gomes Macedo
 * Matricula........: 202210166
 * Inicio...........: 26/03/2024
 * Ultima alteracao.: 07/04/2024
 * Nome.............: CamadaFisicaTransmissora
 * Funcao...........: Codifica os dados e envia para o meio de
 *                    comunicacao.
 *************************************************************** */
package model;

import controller.MainController;

import java.util.ArrayList;
import java.util.Arrays;

public class CamadaFisicaTransmissora {
  static int[] fluxoDeBits;

  /* ***************************************************************
   * Metodo: codificaQuadro
   * Funcao: Codificar, com base na escolha, um quadro de bits.
   * Parametros: vetor de inteiros
   * Retorno: sem retorno.
   *************************************************************** */
  public static void codificaQuadro(int[] quadro) {
    switch (MainController.getTipoDeCodificacao()) {
      case 1:
        fluxoDeBits = codificaBinario(quadro);
        break;
      case 2:
        fluxoDeBits = codificaManchester(quadro);
        break;
      case 3:
        fluxoDeBits = codificaManchesterDiferencial(quadro);
        break;
    }
    if (MainController.getTipoDeEnquadramento() != 4) {
      MainController.meioDeComunicacao.recebe(fluxoDeBits);
      return;
    }
    violacaoCamadaFisicaTransmissora(fluxoDeBits);
  }

  /* ***************************************************************
   * Metodo: codificaBinario
   * Funcao: Codificar as informacoes enviadas no quadro.
   * Parametros: vetor de inteiros
   * Retorno: Vetor de inteiros contendo o quadro de bits codificado
   *************************************************************** */
  public static int[] codificaBinario(int[] quadro) {
    //ja esta em binario, so retorna.
    return quadro;
  }

  /* ***************************************************************
   * Metodo: codificaMacnhester
   * Funcao: Codificar as informacoes enviadas no quadro.
   * Parametros: vetor de inteiros, inteiro contendo o tamanho da mensagem.
   * Retorno: Vetor de inteiros contendo o quadro de bits codificado
   *************************************************************** */
  public static int[] codificaManchester(int[] quadro) {
    //DIMENSIONANDO O VETOR MANCH
    int indexQuadro = 0;
    int indexFluxoDeBits = 0;
    int bitPositionManchester = 1;//era 15
    int bitPosition = 0;// era 7
    int tam;
    int qtdCarac = MainController.bitsNoQuadro / 8;
    if (MainController.bitsNoQuadro % 8 != 0) {
      qtdCarac++; // Arredondar para cima se não for múltiplo de 8
    }
    if (qtdCarac % 2 == 0) {
      tam = qtdCarac / 2;
    } else {
      tam = (qtdCarac / 2) + 1;
    }
    int[] fluxoDeBits = new int[tam];
    for (int i = 0; i < MainController.bitsNoQuadro; i++) {//tam se refere ao tamanho da mensagem, pois, como o bit 0 eh codificado em 01, caso eu tenha um for maior que o tamanho da mensagem, estarei codificando todos os bits 0 de posicoes vazias com 01.
      if (bitPosition == 32) {
        indexQuadro++;
        bitPosition = 0;
      }
      if (bitPositionManchester > 32) {
        indexFluxoDeBits++;
        bitPositionManchester = 1;
      }
      int bit = Math.abs((quadro[indexQuadro] & (1 << bitPosition)) >> bitPosition);
      if (bit == 1) {//1 0
        fluxoDeBits[indexFluxoDeBits] |= bit << bitPositionManchester;
      } else {//0 1
        fluxoDeBits[indexFluxoDeBits] |= 1 << bitPositionManchester - 1;
      }
      bitPosition++;
      bitPositionManchester += 2;

    }

    MainController.bitsNoQuadro *= 2;
    return fluxoDeBits;
  }

  /* ***************************************************************
   * Metodo: codificaManchesterDiferencial
   * Funcao: Codificar as informacoes enviadas no quadro.
   * Parametros: vetor de inteiros
   * Retorno: Vetor de inteiros contendo o quadro de bits codificado
   *************************************************************** */
  public static int[] codificaManchesterDiferencial(int[] quadro) {
    int[] fluxoDeBits;
    int indexFluxoDeBits = 0;
    int indexQuadro = 0;
    int bitPositionManchDif = 0;
    int bitPosition = 0;
    int verificaAnterior = 1;
    int tam;
    int qtdCarac = MainController.bitsNoQuadro / 8;
    int fim = MainController.bitsNoQuadro;
    if (fim % 32 == 0) {
      tam = qtdCarac / 2;
    } else {
      tam = (qtdCarac / 2) + 1;
    }
    fluxoDeBits = new int[tam];
    for (int i = 0; i < fim; i++) {//tam se refere ao tamanho da mensagem, pois, como o bit 0 eh codificado em 01, caso eu tenha um for maior que o tamanho da mensagem, estarei codificando todos os bits 0 de posicoes vazias com 01.
      if (i % 16 == 0 && MainController.getTipoDeEnquadramento() == 4) {//condicao especial para manter os bits setados corretamente ao enquadrar como violacao de camada fisica.
        verificaAnterior = 1;
      }
      if (bitPosition == 32) {
        indexQuadro++;
        bitPosition = 0;
      }
      if (bitPositionManchDif == 32) {
        indexFluxoDeBits++;//aumentando o indice, pois ja guardou info de dois caracteres em um int.
        bitPositionManchDif = 0;//retomando a posicao para setar a mascara corretamente e guardar info de 1 caracter em 16 bits.
      }
      int mask = 1 << bitPosition;
      int bitInfo = (mask & quadro[indexQuadro]) >> bitPosition;
      if (bitInfo != 0) {//pode ser -1
        fluxoDeBits[indexFluxoDeBits] = fluxoDeBits[indexFluxoDeBits] | (verificaAnterior) << bitPositionManchDif;
        fluxoDeBits[indexFluxoDeBits] = fluxoDeBits[indexFluxoDeBits] | (1 - verificaAnterior) << bitPositionManchDif + 1;
        verificaAnterior = 1 - verificaAnterior;
      } else {
        fluxoDeBits[indexFluxoDeBits] = fluxoDeBits[indexFluxoDeBits] | (1 - verificaAnterior) << bitPositionManchDif;
        fluxoDeBits[indexFluxoDeBits] = fluxoDeBits[indexFluxoDeBits] | (verificaAnterior) << bitPositionManchDif + 1;
      }
      bitPosition++;
      bitPositionManchDif += 2;
    }
    MainController.bitsNoQuadro *= 2;
    return fluxoDeBits;

  }


  /* ***************************************************************
   * Metodo: violacaoCamadaFisicaTransmissora
   * Funcao: Codificar as informacoes enviadas no quadro usando o
   *         algoritmo de violacao de camada fisica.
   * Parametros: vetor de inteiros
   * Retorno: Vetor de inteiros contendo o quadro de bits codificado
   *************************************************************** */
  private static void violacaoCamadaFisicaTransmissora(int[] fluxoDeBits) {
    int[] quadroEnquadrado;
    int deslocQuadroEnquadrado = 0;
    int deslocFluxoDeBits = 0;
    int indiceFluxo = 0;
    int indiceQuadroEnquadrado = 0;
    int bitsPreenchidosFluxo = MainController.bitsNoQuadro;
    int qtdCaracteresFluxo = bitsPreenchidosFluxo / 16;
    int flags;
    if (qtdCaracteresFluxo % 2 == 0) {
      if (bitsPreenchidosFluxo < 32) {//caso seja uma unica letra
        flags = 2;
      } else {
        flags = (qtdCaracteresFluxo / 2) * 2;
      }
    } else {
      flags = ((qtdCaracteresFluxo / 2) * 2) + 2;
    }
    int tam = ((flags * 3) + bitsPreenchidosFluxo) / 32;
    if (tam % 32 != 0 || ((flags * 3) + bitsPreenchidosFluxo) < 32) {
      tam += 1;
    }
    int countCaracteresEnquadrados = 0;
    quadroEnquadrado = new int[tam];
    MainController.bitsNoQuadro = 0;
    for (int i = 0; i <= qtdCaracteresFluxo; i++) {
      if (i % 2 == 0 && i != 0) {
        indiceFluxo++;
        deslocFluxoDeBits = 0;
      }
      if (countCaracteresEnquadrados % 2 == 0 || countCaracteresEnquadrados == qtdCaracteresFluxo) {// 2 possibilidades: colocar 1 flag caso seja a primeira ou ultima OU colocar duas flags.
        if (countCaracteresEnquadrados == 0 || countCaracteresEnquadrados == qtdCaracteresFluxo) {//colocar uma flag
          for (int j = 0; j < 3; j++) {
            MainController.bitsNoQuadro++;
            if (deslocQuadroEnquadrado == 32) {
              indiceQuadroEnquadrado++;
              deslocQuadroEnquadrado = 0;
            }
            quadroEnquadrado[indiceQuadroEnquadrado] |= 1 << deslocQuadroEnquadrado;
            deslocQuadroEnquadrado++;
          }
          if (i == qtdCaracteresFluxo) {
            break;
          }
        } else {//colocar duas flags
          for (int j = 0; j < 2; j++) {
            for (int k = 0; k < 3; k++) {
              MainController.bitsNoQuadro++;
              if (deslocQuadroEnquadrado == 32) {
                indiceQuadroEnquadrado++;
                deslocQuadroEnquadrado = 0;
              }
              quadroEnquadrado[indiceQuadroEnquadrado] |= 1 << deslocQuadroEnquadrado;
              deslocQuadroEnquadrado++;
            }
            if (j == 0) {
              ControleDeErroVCF(redimensionarVetor(quadroEnquadrado));
              try {
                MainController.quadroEnviado.acquire();
              } catch (InterruptedException e) {
                throw new RuntimeException(e);
              }
              if (MainController.erroDetectado) {
                break;
              }
              deslocQuadroEnquadrado = 0;
              indiceQuadroEnquadrado = 0;
              MainController.bitsNoQuadro = 0;
              Arrays.fill(quadroEnquadrado, 0);
            }
          }
        }
      }
      for (int j = 0; j < 16; j++) {//enquadrar o caractere
        MainController.bitsNoQuadro++;
        if (deslocQuadroEnquadrado == 32) {
          indiceQuadroEnquadrado++;
          deslocQuadroEnquadrado = 0;
        }
        int bit = (fluxoDeBits[indiceFluxo] & 1 << deslocFluxoDeBits) >> deslocFluxoDeBits;
        if (bit == -1) {
          bit = 1;
        }
        deslocFluxoDeBits++;
        quadroEnquadrado[indiceQuadroEnquadrado] |= bit << deslocQuadroEnquadrado;
        deslocQuadroEnquadrado++;
      }
      countCaracteresEnquadrados++;
    }
    if (MainController.erroDetectado) {
      return;
    }
    ControleDeErroVCF(redimensionarVetor(quadroEnquadrado));
    MainController.mensagemCompleta = true;
  }


  //EXTENSAO DE CONTROLE DE ERRO PARA VIOLACAO DE CAMADA FISICA
  static public void ControleDeErroVCF(int quadro[]) {
    int algControleDeErro = MainController.getTipoDeControleDeErro();
    switch (algControleDeErro) {
      case 1:
        ControleDeErroBitParidadeParVCF(quadro);
        break;
      case 2:
        ControleDeErroBitParidadeImparVCF(quadro);
        break;
      case 3:
        ControleDeErroCRCVCF(quadro);
        break;
      case 4:
        ControleDeErroCodigoHammingVCF(quadro);
        break;
    }

  }


  /* ***************************************************************
   * Metodo: ControleDeErroBitParidadeParVCF
   * Funcao: Adicionar controle de erro ao quadro usando o
   *         algoritmo de Bit Paridade Par
   * Parametros: vetor de inteiros
   * Retorno: Sem retorno.
   *************************************************************** */
  static private void ControleDeErroBitParidadeParVCF(int[] quadro) {
    int[] quadroCE;
    int tam = quadro.length;
    int bitsNoQuadro = MainController.bitsNoQuadro;
    if (bitsNoQuadro == (quadro.length * 32)) {
      tam = quadro.length + 1;
    }
    int posicaoBitControle = ((bitsNoQuadro + 1) % 32) - 1;
    quadroCE = new int[tam];
    int count = 0;
    int index = 0;
    int desloc = 0;
    for (int i = 0; i < bitsNoQuadro; i++) {
      if (i % 32 == 0 && i != 0) {
        index++;
        desloc = 0;
      }
      int bit = (quadro[index] & (1 << desloc)) >> desloc;
      quadroCE[index] |= bit << desloc;
      if (bit == 1 || bit == -1) {
        count++;
      }
      desloc++;
    }
    if (count % 2 != 0) {
      quadroCE[quadroCE.length - 1] |= 1 << posicaoBitControle;
    }
    MainController.bitsNoQuadro++;
    MainController.meioDeComunicacao.recebe(quadroCE);
  }

  /* ***************************************************************
   * Metodo: ControleDeErroBitParidadeImparVCF
   * Funcao: Adicionar controle de erro ao quadro usando o
   *         algoritmo de Bit Paridade Impar.
   * Parametros: vetor de inteiros
   * Retorno: Sem retorno.
   *************************************************************** */
  private static void ControleDeErroBitParidadeImparVCF(int[] quadro) {
    int[] quadroCE;
    int tam = quadro.length;
    int bitsNoQuadro = MainController.bitsNoQuadro;
    if (bitsNoQuadro == (quadro.length * 32)) {
      tam = quadro.length + 1;
    }
    int posicaoBitControle = ((bitsNoQuadro + 1) % 32) - 1;
    quadroCE = new int[tam];
    int count = 0;
    int index = 0;
    int desloc = 0;
    for (int i = 0; i < bitsNoQuadro; i++) {
      if (i % 32 == 0 && i != 0) {
        index++;
        desloc = 0;
      }
      int bit = (quadro[index] & (1 << desloc)) >> desloc;
      quadroCE[index] |= bit << desloc;
      if (bit == 1 || bit == -1) {
        count++;
      }
      desloc++;
    }
    if (count % 2 == 0) {
      quadroCE[quadroCE.length - 1] |= 1 << posicaoBitControle;
    }
    MainController.bitsNoQuadro++;
    MainController.meioDeComunicacao.recebe(quadroCE);
  }

  /* ***************************************************************
   * Metodo: ControleDeErroCRCVCF
   * Funcao: Adicionar controle de erro ao quadro usando o
   *         algoritmo de CRC.
   * Parametros: vetor de inteiros
   * Retorno: Sem retorno.
   *************************************************************** */
  private static void ControleDeErroCRCVCF(int[] quadro) {
    int[] quadroCE = new int[quadro.length + 1];
    StringBuilder crc32Polinomio = new StringBuilder("100000100110000010001110110110111");
    StringBuilder mensagem = new StringBuilder();
    int fim = MainController.bitsNoQuadro;
    int desloc = 0;
    int index = 0;
    for (int i = 0; i < fim; i++) {//extraindo os bits de dados do quadro e colocando em uma string
      if (i % 32 == 0 && i != 0) {
        index++;
        desloc = 0;
      }
      int bit = (quadro[index] & 1 << desloc) >> desloc;
      mensagem.append(Math.abs(bit));
      desloc++;
    }
    mensagem.reverse();
    StringBuilder mensagemComCRC = new StringBuilder();//instancia da string que contera os dados + crc
    mensagemComCRC.append(mensagem);
    mensagemComCRC.append(calcularCRC32(mensagem.toString(), crc32Polinomio.toString()));//adicionando o crc
    index = 0;
    desloc = 0;
    for (int i = mensagemComCRC.length() - 1; i >= 0; i--) {//preenchendo um vetor com os bits representados na string
      if (desloc == 32) {
        index++;
        desloc = 0;
      }
      int bit = mensagemComCRC.charAt(i) - '0';
      quadroCE[index] |= (bit << desloc);
      desloc++;
    }
    MainController.bitsNoQuadro += 32;
    MainController.meioDeComunicacao.recebe(quadroCE);
  }

  /* ***************************************************************
   * Metodo: calcularCRC32
   * Funcao: Calcular o CRC
   * Parametros: String contendo os dados da mensagem e o polinomio gerador
   * Retorno: String contendo o valor de CRC
   *************************************************************** */
  public static String calcularCRC32(String msg, String crc) {
    int n = crc.length();
    StringBuilder encoded = new StringBuilder(msg);
    for (int i = 0; i < n - 1; i++) {//adiciona 0 ao final da mensagem, onde, posteriormente sera colocado o valor de CRC.
      encoded.append('0');
    }
    for (int i = 0; i <= encoded.length() - n; ) {//realizando a divisao utilizando a operacao XOR
      for (int j = 0; j < n; j++) {
        encoded.setCharAt(i + j, encoded.charAt(i + j) == crc.charAt(j) ? '0' : '1');
      }
      while (i < encoded.length() && encoded.charAt(i) != '1') {//avanca para o proximo bit 1.
        i++;
      }
    }
    return encoded.substring(encoded.length() - n + 1); //retorna o valor do CRC.

  }

  /* ***************************************************************
   * Metodo: ControleDeErroCodigoHammingVCF
   * Funcao: Adicionar controle de erro ao quadro usando o
   *         algoritmo de Codigo de Hamming.
   * Parametros: vetor de inteiros
   * Retorno: Sem retorno.
   *************************************************************** */
  public static void ControleDeErroCodigoHammingVCF(int[] quadro) {
    StringBuilder mensagem = new StringBuilder();
    int fim = MainController.bitsNoQuadro;
    int desloc = 0;
    int index = 0;
    for (int i = 0; i < fim; i++) {//colocando os bits do quadro em uma string
      if (i % 32 == 0 && i != 0) {
        index++;
        desloc = 0;
      }
      int bit = (quadro[index] & 1 << desloc) >> desloc;
      mensagem.append(Math.abs(bit));
      desloc++;
    }
    mensagem.reverse();
    int m = mensagem.length();//bits de dados
    int r = 0;//bits de controle
    while (Math.pow(2, r) < (m + r + 1)) {//calculando a quantidade necessaria de bits de controle
      r++;
    }
    int totalBits = m + r;//total de bits
    int tam = totalBits % 32 == 0 ? totalBits / 32 : (totalBits / 32 + 1);
    int[] quadroCE = new int[tam];
    char[] codigoHamming = new char[totalBits];
    ArrayList<Integer> posicoesBitDeControle = new ArrayList<>();
    Arrays.fill(codigoHamming, '0');
    int j = 0;
    for (int i = 1; i <= totalBits; i++) {//inserindo os bits de dados nos lugares corretos
      if ((i & (i - 1)) == 0) {//deixar como 0 quando for potencia de 2
        posicoesBitDeControle.add(i - 1);//adicionar ao array de posicoes de bit de controle
      } else {
        codigoHamming[i - 1] = mensagem.charAt(j);//adicionar os dados
        j++;
      }
    }
    for (int i : posicoesBitDeControle) {//calculando a paridade dos bits de controle
      int paridadeBitDeControle = calcularParidadeBitDeControle(codigoHamming, i);
      codigoHamming[i] = (char) (paridadeBitDeControle + '0');
    }
    StringBuilder mensagemComHamming = new StringBuilder(new String(codigoHamming));
    index = 0;
    desloc = 0;
    for (int i = mensagemComHamming.length() - 1; i >= 0; i--) {//preenchendo um vetor de inteiros com os bits do codigo de hamming
      if (desloc == 32) {
        index++;
        desloc = 0;
      }
      int bit = mensagemComHamming.charAt(i) - '0';
      quadroCE[index] |= (bit << desloc);
      desloc++;
    }
    MainController.bitsNoQuadro = totalBits;
    MainController.meioDeComunicacao.recebe(quadroCE);
  }

  /* ***************************************************************
   * Metodo: calcularParidadeBitDeControle
   * Funcao: Calcular a paridade do bit de controle dada sua posicao
   * Parametros: vetor com os bits de dados e a posicao do bit de controle
   * Retorno: paridade do bit de controle
   *************************************************************** */
  private static int calcularParidadeBitDeControle(char[] codigoHamming, int paridadeBitDeControle) {
    int paridade = 0;
    paridadeBitDeControle++;  // ajustando para a indexacao Hamming
    for (int i = paridadeBitDeControle; i <= codigoHamming.length; i++) {//iterando sobre os bits da mensagem
      if (((i >> (Integer.numberOfTrailingZeros(paridadeBitDeControle))) & 1) == 1) {
        if (codigoHamming[i - 1] == '1') { //atualizar a paridade
          paridade ^= 1;
        }
      }
    }
    return paridade;
  }


  static int[] redimensionarVetor(int[] vetorOriginal) {
    int[] novoVetor;
    int posicoes = 0;
    for (int i = 0; i < vetorOriginal.length; i++) {
      if (vetorOriginal[i] != 0) {
        posicoes++;
      }
    }
    novoVetor = new int[posicoes];
    int index = 0;
    for (int i = 0; i < vetorOriginal.length; i++) {
      if (vetorOriginal[i] != 0) {
        novoVetor[index] = vetorOriginal[i];
        index++;
      }
    }
    return novoVetor;
  }
}
