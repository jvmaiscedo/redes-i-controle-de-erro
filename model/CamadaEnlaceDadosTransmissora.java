/* ***************************************************************
 * Autor............: Joao Victor Gomes Macedo
 * Matricula........: 202210166
 * Inicio...........: 29/04/2024
 * Ultima alteracao.: 02/06/2024
 * Nome.............: CamadaEnlaceDadosTransmissora
 * Funcao...........: Enquadra o quadro e envia para a camada fisica
 *                    transmissora
 *************************************************************** */
package model;

import controller.MainController;

import java.util.ArrayList;
import java.util.Arrays;

public class CamadaEnlaceDadosTransmissora {


  /* ***************************************************************
   * Metodo: CamadaEnlaceDadosTransmissora
   * Funcao: Instancia uma thread que ira enquadrar quadro por quadro
   *         e encaminha-los ao controle de erro.
   * Parametros: quadro contendo a carga util.
   * Retorno: Sem retorno.
   *************************************************************** */

  public static void CamadaEnlaceDadosTransmissora(int quadro[]) {
    new Thread(() -> {
      CamadaDeEnlaceTransmissoraEnquadramento(quadro);
    }).start();
  }

  /* ***************************************************************
   * Metodo: camadaDeEnlaceTransmissoraEnquadramento
   * Funcao: Enquadrar o quadro de bits com base no algoritmo de
   *         enquadramento escolhido e encaminhar para a camada
   *         inferior.
   * Parametros: Quadro com bits
   * Retorno: Sem retorno.
   *************************************************************** */
  static public void CamadaDeEnlaceTransmissoraEnquadramento(int quadro[]) {
    MainController.mensagemCompleta = false;
    int tipoDeEnquadramento = MainController.getTipoDeEnquadramento(); //alterar de acordo com o teste
    int[] quadroEnquadrado;
    switch (tipoDeEnquadramento) {
      case 1: //contagem de caracteres
        CamadaDeEnlaceTransmissoraEnquadramentoContagemDeCaracteres(quadro);
        break;
      case 2: //insercao de bytes
        CamadaDeEnlaceTransmissoraEnquadramentoInsercaoDeBytes(quadro);
        break;
      case 3:
        CamadaDeEnlaceTransmissoraEnquadramentoInsercaoDeBits(quadro);
        break;
      case 4: //violacao da camada fisica
        CamadaDeEnlaceTransmissoraEnquadramentoViolacaoCamadaFisica(quadro);
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + tipoDeEnquadramento);
    }
  }

  /* ***************************************************************
   * Metodo: CamadaDeEnlaceTransmissoraEnquadramentoContagemDeCaracteres
   * Funcao: Enquadrar o quadro de bits utilizando o algoritmo de
   *         contagem de caracteres.
   * Parametros: Quadro com bits.
   * Retorno: Sem retorno
   *************************************************************** */
  private static void CamadaDeEnlaceTransmissoraEnquadramentoContagemDeCaracteres(int[] quadro) {
    int tamanhoQuadro = 3;
    int deslocQuadro = 7;
    int auxDeslocQuadro = 0;
    int indiceQuadro = 0;
    int deslocQuadroEnquadrado = 7;
    int auxDeslocQuadroEnquadrado = 0;
    int indiceQuadroEnquadrado = 0;
    int bitInfo;
    int flag;
    int qtdCarac = MainController.bitsNaMensagem / 8;
    int[] quadroEnquadrado = new int[1];
    for (int i = 0; i < quadro.length * 32; i++) {

      if (i % 32 == 0 && i != 0) {
        indiceQuadro++;
        deslocQuadro = 7;
      } else {
        if (auxDeslocQuadro % 8 == 0 && i != 0) {
          deslocQuadro += 16;
        }
      }
      if (i % 8 == 0) {
        if (deslocQuadro == 7) {
          flag = (quadro[indiceQuadro]) & 255;
        } else {
          flag = (quadro[indiceQuadro] >> (deslocQuadro - 7)) & 255;
        }
        if (flag == 0) {
          break;
        }
      }
      if (i % 24 == 0) {
        if (i != 0) {
          camadaDeEnlaceTransmissoraControleDeErro(quadroEnquadrado);
          try {
            MainController.quadroEnviado.acquire();
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
          if (MainController.erroDetectado) {
            break;
          }
          MainController.bitsNoQuadro = 0;
          deslocQuadroEnquadrado = 7;
          auxDeslocQuadroEnquadrado = 0;
          indiceQuadroEnquadrado = 0;
          Arrays.fill(quadroEnquadrado, 0);
        }
        int indiceContagem = (Math.min(tamanhoQuadro, qtdCarac - (i / 8))) + 1;
        for (int j = 7; j >= 0; j--) {
          MainController.bitsNoQuadro++;
          int mask = indiceContagem >> j;
          quadroEnquadrado[indiceQuadroEnquadrado] |= (mask & 1) << deslocQuadroEnquadrado;
          deslocQuadroEnquadrado--;
          auxDeslocQuadroEnquadrado++;
        }
      }
      if (auxDeslocQuadroEnquadrado % 8 == 0 && auxDeslocQuadroEnquadrado != 0) {
        deslocQuadroEnquadrado += 16;
      }
      bitInfo = (quadro[indiceQuadro] & (1 << deslocQuadro)) >> deslocQuadro;
      if (bitInfo == -1)
        bitInfo = 1;
      MainController.bitsNoQuadro++;
      quadroEnquadrado[indiceQuadroEnquadrado] |= (bitInfo) << deslocQuadroEnquadrado;
      deslocQuadroEnquadrado--;
      deslocQuadro--;
      auxDeslocQuadroEnquadrado++;
      auxDeslocQuadro++;
    }
    if (MainController.erroDetectado) {
      return;
    }
    camadaDeEnlaceTransmissoraControleDeErro(quadroEnquadrado);
    MainController.mensagemCompleta = true;
  }


  /* ***************************************************************
   * Metodo: CamadaDeEnlaceTransmissoraEnquadramentoInsercaoDeBytes
   * Funcao: Enquadrar o quadro de bits utilizando o algoritmo de
   *         insercao de bytes.
   * Parametros: Quadro com bits.
   * Retorno: Sem retorno
   *************************************************************** */
  private static void CamadaDeEnlaceTransmissoraEnquadramentoInsercaoDeBytes(int[] quadro) {
    //cada quadro sera composto por FLAG A B C D FLAG;
    int deslocQuadro = 7;
    int auxDeslocQuadro = 0;
    int indiceQuadro = 0;
    int deslocQuadroEnquadrado = 7;
    int auxDeslocQuadroEnquadrado = 0;
    int indiceQuadroEnquadrado = 0;
    int qtdCarac = MainController.bitsNaMensagem / 8;
    int[] quadroEnquadrado = new int[3];
    //PROTOCOLO
    int flag = 87;// 87 eh referente a palavra W, que, segundo pesquisa, tem incidencia de 0,01% na lingua portuguesa.
    int esc = 36;// 36 eh referente ao simbolo $, sera usado como ESCAPE
    //FIM INFO PROTOCOLO
    int caracteresEnquadrados = 0; //Aqui para controle dos caracteres - para saber quando colocar flag de inicio e fim
    int bytesNoQuadroEnquadrado = 0;//aqui para controle do indice do quadroEnquadrado

    for (int i = 0; i <= qtdCarac; i++) {
      if (i % 4 == 0 && i != 0) {
        indiceQuadro++;
        deslocQuadro = 7;
      } else {
        if (auxDeslocQuadro % 8 == 0 && i != 0) {
          deslocQuadro += 16;
        }
      }
      if (caracteresEnquadrados % 2 == 0 || i == qtdCarac) {//coloca flags de acordo a quantidade de caracteres desejada por quadro (neste caso, 4).
        if (i == 0 || i == qtdCarac) {
          if (auxDeslocQuadroEnquadrado % 8 == 0 && auxDeslocQuadroEnquadrado != 0) {
            if (auxDeslocQuadroEnquadrado % 32 == 0) {
              deslocQuadroEnquadrado = 7;
            } else {
              deslocQuadroEnquadrado += 16;
            }
          }
          if (bytesNoQuadroEnquadrado % 4 == 0 && i != 0) {
            indiceQuadroEnquadrado++;
          }
          for (int j = 7; j >= 0; j--) {
            int mask = flag >> j;
            quadroEnquadrado[indiceQuadroEnquadrado] |= (mask & 1) << deslocQuadroEnquadrado;
            MainController.bitsNoQuadro++;
            deslocQuadroEnquadrado--;
            auxDeslocQuadroEnquadrado++;
          }
          bytesNoQuadroEnquadrado++;
          if (i == qtdCarac) {
            break;
          }
        } else {
          for (int j = 0; j < 2; j++) {//colocando duas FLAGS
            if (auxDeslocQuadroEnquadrado % 8 == 0 && auxDeslocQuadroEnquadrado != 0) {
              if (auxDeslocQuadroEnquadrado % 32 == 0) {
                deslocQuadroEnquadrado = 7;
              } else {
                deslocQuadroEnquadrado += 16;
              }
            }
            if (bytesNoQuadroEnquadrado % 4 == 0 && i != 0) {
              indiceQuadroEnquadrado++;
            }
            for (int k = 7; k >= 0; k--) {
              int mask = flag >> k;
              quadroEnquadrado[indiceQuadroEnquadrado] |= (mask & 1) << deslocQuadroEnquadrado;
              MainController.bitsNoQuadro++;
              deslocQuadroEnquadrado--;
              auxDeslocQuadroEnquadrado++;
            }
            bytesNoQuadroEnquadrado++;
            if (j == 0) {
              camadaDeEnlaceTransmissoraControleDeErro(redimensionarVetor(quadroEnquadrado));
              try {
                MainController.quadroEnviado.acquire();
              } catch (InterruptedException e) {
                throw new RuntimeException(e);
              }
              if (MainController.erroDetectado) {
                break;
              }
              deslocQuadroEnquadrado = 7;
              auxDeslocQuadroEnquadrado = 0;
              indiceQuadroEnquadrado = 0;
              MainController.bitsNoQuadro = 0;
              Arrays.fill(quadroEnquadrado, 0);
            }
          }
        }
      }
      int aux = 0;
      for (int j = 7; j >= 0; j--) {//fazendo a contagem de bits do caractere
        aux += (quadro[indiceQuadro] & (1 << deslocQuadro));
        deslocQuadro--;
        auxDeslocQuadro++;
      }
      aux = aux >> deslocQuadro + 1;
      if (aux == flag || aux == esc) {//verificando se precisa de ESC
        if (auxDeslocQuadroEnquadrado % 8 == 0 && auxDeslocQuadroEnquadrado != 0) {
          if (auxDeslocQuadroEnquadrado % 32 == 0) {
            deslocQuadroEnquadrado = 7;
          } else {
            deslocQuadroEnquadrado += 16;
          }
        }
        if (bytesNoQuadroEnquadrado % 4 == 0 && i != 0) {
          indiceQuadroEnquadrado++;
        }
        for (int j = 7; j >= 0; j--) {//Colocando ESC, caso precise
          int mask = esc >> j;
          quadroEnquadrado[indiceQuadroEnquadrado] |= (mask & 1) << deslocQuadroEnquadrado;
          MainController.bitsNoQuadro++;
          deslocQuadroEnquadrado--;
          auxDeslocQuadroEnquadrado++;
        }
        bytesNoQuadroEnquadrado++;
      }//Colocando o caractere
      if (auxDeslocQuadroEnquadrado % 8 == 0 && auxDeslocQuadroEnquadrado != 0) {
        if (auxDeslocQuadroEnquadrado % 32 == 0) {
          deslocQuadroEnquadrado = 7;
        } else {
          deslocQuadroEnquadrado += 16;
        }
      }
      if (bytesNoQuadroEnquadrado % 4 == 0 && i != 0) {
        indiceQuadroEnquadrado++;
      }
      quadroEnquadrado[indiceQuadroEnquadrado] += aux << deslocQuadroEnquadrado - 7;
      MainController.bitsNoQuadro += 8;
      caracteresEnquadrados++;
      bytesNoQuadroEnquadrado++;
      deslocQuadroEnquadrado -= 8;
      auxDeslocQuadroEnquadrado += 8;
    }
    if (MainController.erroDetectado) {
      return;
    }
    camadaDeEnlaceTransmissoraControleDeErro(redimensionarVetor(quadroEnquadrado));
    MainController.mensagemCompleta = true;
  }

  /* ***************************************************************
   * Metodo: CamadaDeEnlaceTransmissoraEnquadramentoInsercaoDeBits
   * Funcao: Enquadrar o quadro de bits utilizando o algoritmo de
   *         insercao de bits.
   * Parametros: Quadro com bits.
   * Retorno: Sem retorno
   *************************************************************** */
  private static void CamadaDeEnlaceTransmissoraEnquadramentoInsercaoDeBits(int[] quadro) {
    int[] quadroEnquadrado = new int[3];
    int flag = 126;
    int deslocQuadro = 0;
    int indexQuadro = 0;
    int deslocQuadroEnquadrado = 0;
    int indexQuadroEnquadrado = 0;
    int contadorBit = 0;
    int fim = MainController.bitsNaMensagem;
    for (int i = 0; i < fim; i++) {
      if (i % 32 == 0 && i != 0) {
        indexQuadro++;
        deslocQuadro = 0;
      }

      if (i % 24 == 0) {
        if (i != 0) {
          for (int k = 7; k >= 0; k--) {
            quadroEnquadrado[indexQuadroEnquadrado] |= ((flag >> k) & 1) << deslocQuadroEnquadrado;
            MainController.bitsNoQuadro++;
            deslocQuadroEnquadrado++;
            if (deslocQuadroEnquadrado == 32) {
              indexQuadroEnquadrado++;
              deslocQuadroEnquadrado = 0;
            }
          }
          camadaDeEnlaceTransmissoraControleDeErro(redimensionarVetor(quadroEnquadrado));
          try {
            MainController.quadroEnviado.acquire();
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
          if (MainController.erroDetectado) {
            break;
          }
          deslocQuadroEnquadrado = 0;
          indexQuadroEnquadrado = 0;
          Arrays.fill(quadroEnquadrado, 0);
          MainController.bitsNoQuadro = 0;
        }
        for (int k = 7; k >= 0; k--) {
          quadroEnquadrado[indexQuadroEnquadrado] |= ((flag >> k) & 1) << deslocQuadroEnquadrado;
          MainController.bitsNoQuadro++;
          deslocQuadroEnquadrado++;
          if (deslocQuadroEnquadrado == 32) {
            indexQuadroEnquadrado++;
            deslocQuadroEnquadrado = 0;
          }

        }
      }

      int bitInfo = (quadro[indexQuadro] & (1 << deslocQuadro)) >> deslocQuadro;
      bitInfo = Math.abs(bitInfo);
      quadroEnquadrado[indexQuadroEnquadrado] |= (bitInfo) << deslocQuadroEnquadrado;
      MainController.bitsNoQuadro++;
      if (bitInfo == 1) {
        contadorBit++;
        if (contadorBit == 5) {
          deslocQuadroEnquadrado++;
          MainController.bitsNoQuadro++;
          if (deslocQuadroEnquadrado == 32) {
            indexQuadroEnquadrado++;
            deslocQuadroEnquadrado = 0;
          }
        }
      } else {
        contadorBit = 0;
      }

      deslocQuadro++;
      deslocQuadroEnquadrado++;

      if (deslocQuadroEnquadrado == 32) {
        indexQuadroEnquadrado++;
        deslocQuadroEnquadrado = 0;
      }
      if (i == fim - 1) {
        for (int j = 7; j >= 0; j--) {
          quadroEnquadrado[indexQuadroEnquadrado] |= ((flag >> j) & 1) << deslocQuadroEnquadrado;
          MainController.bitsNoQuadro++;
          deslocQuadroEnquadrado++;
          if (deslocQuadroEnquadrado == 32) {
            indexQuadroEnquadrado++;
            deslocQuadroEnquadrado = 0;
          }
        }
      }
    }
    if (MainController.erroDetectado) {
      return;
    }
    camadaDeEnlaceTransmissoraControleDeErro(redimensionarVetor(quadroEnquadrado));
    MainController.mensagemCompleta = true;
  }

  /* ***************************************************************
   * Metodo: CamadaDeEnlaceTransmissoraEnquadramentoViolacaoCamadaFisica
   * Funcao: Enquadrar o quadro de bits utilizando o algoritmo de
   *         violacao de camada fisica.
   * Parametros: Quadro com bits.
   * Retorno: sem retorno
   *************************************************************** */
  private static void CamadaDeEnlaceTransmissoraEnquadramentoViolacaoCamadaFisica(int[] quadro) {
    MainController.bitsNoQuadro = MainController.bitsNaMensagem;
    CamadaFisicaTransmissora.codificaQuadro(quadro);
  }


  //----------------------------------CONTROLE DE ERRO-----------------------------------------------//

  /* ***************************************************************
   * Metodo: CamadaDeEnlaceTransmissoraEnquadramentoViolacaoCamadaFisica
   * Funcao: Adicionar controle de erro ao quadro de bits utilizando
   *         o algoritmo de deteccao de erro selecionado na interface.
   * Parametros: quadro contendo carga util e bits do enquadramento.
   * Retorno: sem retorno
   *************************************************************** */
  static public void camadaDeEnlaceTransmissoraControleDeErro(int quadro[]) {
    int algControleDeErro = MainController.getTipoDeControleDeErro();
    switch (algControleDeErro) {
      case 1:
        CamadaDeEnlaceTransmissoraControleDeErroBitParidadePar(quadro);
        break;
      case 2:
        CamadaDeEnlaceTransmissoraControleDeErroBitParidadeImpar(quadro);
        break;
      case 3:
        CamadaDeEnlaceTransmissoraControleDeErroCRC(quadro);
        break;
      case 4:
        CamadaDeEnlaceTransmissoraControleDeErroCodigoHamming(quadro);
        break;
      default:
    }

  }


  /* ***************************************************************
   * Metodo: CamadaDeEnlaceTransmissoraControleDeErroBitParidadePar
   * Funcao: Adicionar controle de erro ao quadro de bits utilizando
   *         o algoritmo de deteccao de erro Bit Paridade Par.
   * Parametros: quadro contendo carga util e bits do enquadramento.
   * Retorno: sem retorno
   *************************************************************** */
  static private void CamadaDeEnlaceTransmissoraControleDeErroBitParidadePar(int[] quadro) {
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
    CamadaFisicaTransmissora.codificaQuadro(quadroCE);
  }

  /* ***************************************************************
   * Metodo: CamadaDeEnlaceTransmissoraControleDeErroBitParidadeImpar
   * Funcao: Adicionar controle de erro ao quadro de bits utilizando
   *         o algoritmo de deteccao de erro Bit Paridade Impar.
   * Parametros: quadro contendo carga util e bits do enquadramento.
   * Retorno: sem retorno
   *************************************************************** */
  private static void CamadaDeEnlaceTransmissoraControleDeErroBitParidadeImpar(int[] quadro) {
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
    CamadaFisicaTransmissora.codificaQuadro(quadroCE);
  }

  /* ***************************************************************
   * Metodo: CamadaDeEnlaceTransmissoraControleDeErroCRC
   * Funcao: Adicionar controle de erro ao quadro de bits utilizando
   *         o algoritmo de deteccao de erro CRC.
   * Parametros: quadro contendo carga util e bits do enquadramento.
   * Retorno: sem retorno
   *************************************************************** */
  private static void CamadaDeEnlaceTransmissoraControleDeErroCRC(int[] quadro) {
    int[] quadroCE = new int[quadro.length + 1];
    StringBuilder crc32Polinomio = new StringBuilder("100000100110000010001110110110111");
    StringBuilder mensagem = new StringBuilder();
    int fim = MainController.bitsNoQuadro;
    int desloc = 0;
    int index = 0;
    for (int i = 0; i < fim; i++) {//adiciona os bits da carga util em uma string para o calculo do crc
      if (i % 32 == 0 && i != 0) {
        index++;
        desloc = 0;
      }
      int bit = (quadro[index] & 1 << desloc) >> desloc;
      mensagem.append(Math.abs(bit));
      desloc++;
    }
    mensagem.reverse();
    StringBuilder mensagemComCRC = new StringBuilder();//inicia a string que contera os dados e o crcc
    mensagemComCRC.append(mensagem);
    mensagemComCRC.append(calcularCRC32(mensagem.toString(), crc32Polinomio.toString()));//adiciona o crc
    index = 0;
    desloc = 0;
    for (int i = mensagemComCRC.length() - 1; i >= 0; i--) {//coloca os bits representados na string em um vetor de inteiros
      if (desloc == 32) {
        index++;
        desloc = 0;
      }
      int bit = mensagemComCRC.charAt(i) - '0';
      quadroCE[index] |= (bit << desloc);
      desloc++;
    }
    MainController.bitsNoQuadro += 32;
    CamadaFisicaTransmissora.codificaQuadro(quadroCE);//encaminha para a camada fisica o quadro com controle de erro.
  }

  /* ***************************************************************
   * Metodo: calcularCRC32
   * Funcao: Calcular o valor do CRC adicionado a carga util
   * Parametros: String contendo os bits da mensagem e String
   *             contendo os bits do polinomio gerador.
   * Retorno: String contendo o CRC.
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
   * Metodo: CamadaDeEnlaceTransmissoraControleDeErroCodigoHamming
   * Funcao: Adicionar controle de erro ao quadro de bits utilizando
   *         o algoritmo de deteccao de erro Codigo Hamming.
   * Parametros: quadro contendo carga util e bits do enquadramento.
   * Retorno: sem retorno
   *************************************************************** */
  public static void CamadaDeEnlaceTransmissoraControleDeErroCodigoHamming(int[] quadro) {
    StringBuilder mensagem = new StringBuilder();
    int fim = MainController.bitsNoQuadro;
    int desloc = 0;
    int index = 0;
    for (int i = 0; i < fim; i++) {//extraindo os bits da mensagem e colocando em uma string.
      if (i % 32 == 0 && i != 0) {
        index++;
        desloc = 0;
      }
      int bit = (quadro[index] & 1 << desloc) >> desloc;
      mensagem.append(Math.abs(bit));
      desloc++;
    }
    mensagem.reverse();
    int m = mensagem.length();// quantidade de bits de dados
    int r = 0;// quantidade de bits de paridade
    while (Math.pow(2, r) < (m + r + 1)) {//calculando a quantidade de bits de controle necessarios.
      r++;
    }
    int totalBits = m + r;// Total de bits na mensagem codificada (bits de dados + bits de paridade)
    int tam = totalBits % 32 == 0 ? totalBits / 32 : (totalBits / 32 + 1);
    int[] quadroCE = new int[tam];
    char[] hammingCode = new char[totalBits];// vetor de char para armazenar a mensagem codificada
    ArrayList<Integer> posicaoBitsDeControle = new ArrayList<>();
    Arrays.fill(hammingCode, '0');// preenchimento do array com 0
    int j = 0;
    for (int i = 1; i <= totalBits; i++) {    // inserindo dos bits de dados nos lugares corretos
      if ((i & (i - 1)) == 0) {//se for potencia de 2, entao eh bit de controle
        posicaoBitsDeControle.add(i - 1);
      } else {//se nao for, entao eh um bit de dado
        hammingCode[i - 1] = mensagem.charAt(j);
        j++;
      }
    }
    for (int i : posicaoBitsDeControle) {    //calculando a paridade dos bits de controle
      int paridadeBit = calcularBitParidade(hammingCode, i);
      hammingCode[i] = (char) (paridadeBit + '0');
    }

    StringBuilder mensagemComHamming = new StringBuilder(new String(hammingCode));//salvando o codigo em uma string para inserir os bits em um vetor de inteiros.
    index = 0;
    desloc = 0;
    for (int i = mensagemComHamming.length() - 1; i >= 0; i--) {//preenchendo o vetos com os bits do codigo hamming gerado.
      if (desloc == 32) {
        index++;
        desloc = 0;
      }
      int bit = mensagemComHamming.charAt(i) - '0';
      quadroCE[index] |= (bit << desloc);
      desloc++;
    }
    MainController.bitsNoQuadro = totalBits;
    CamadaFisicaTransmissora.codificaQuadro(quadroCE);//enviado o resultado do controle de erro para a camada fisica.
  }

  /* ***************************************************************
   * Metodo: calcularBitParidade
   * Funcao: Calcular a paridade do bit de controle baseado nos bits
   *         de dados cobertos por ele.
   * Parametros: vetor com os bits de dados e a posicao do bit de controle
   *             que tera o valor de paridade calculado.
   * Retorno: inteiro contendo o valor do bit de controle.
   *************************************************************** */
  private static int calcularBitParidade(char[] codigoHamming, int posicaoBitDeControle) {
    int valorParidade = 0;
    posicaoBitDeControle++;  // ajustando para a indexacao hamming

    for (int i = posicaoBitDeControle; i <= codigoHamming.length; i++) {//iterando sobre os bits da mensagem
      if (((i >> (Integer.numberOfTrailingZeros(posicaoBitDeControle))) & 1) == 1) {//se o bit for coberto pelo bit de controle
        if (codigoHamming[i - 1] == '1') {//se for 1 realizar um XOR que por fim resultara em qual valor de paridade deve atribuir ao bit de controle.
          valorParidade ^= 1;
        }
      }
    }
    return valorParidade;
  }

  /* ***************************************************************
   * Metodo: redimensionarVetor
   * Funcao: Retirar os inteiros sem informação do vetor.
   * Parametros: Quadro com bits.
   * Retorno: Vetor redimensionado.
   *************************************************************** */
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
