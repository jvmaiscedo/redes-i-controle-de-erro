/* ***************************************************************
 * Autor............: Joao Victor Gomes Macedo
 * Matricula........: 202210166
 * Inicio...........: 29/04/2024
 * Ultima alteracao.: 02/06/2024
 * Nome.............: CamadaEnlaceDadosReceptora
 * Funcao...........: Desenquadra o quadro e envia para a camada de
 *                    aplicacao receptora.
 *************************************************************** */
package model;

import controller.MainController;

public class CamadaEnlaceDadosReceptora {
  /* ***************************************************************
   * Metodo: CamadaDeEnlaceReceptoraDesenquadramento
   * Funcao: Retira a informacao de controle de enquadramento dado um
   *         quadro como parametro.
   * Parametros: quadro enquadrado como vetor de inteiros
   * Retorno: Sem retorno.
   *************************************************************** */
  static void CamadaDeEnlaceReceptoraDesenquadramento(int[] quadro) {
    int tipoDeEnquadramento = MainController.getTipoDeEnquadramento(); //alterar de acordo com o teste
    int[] quadroEnquadrado;
    switch (tipoDeEnquadramento) {
      case 1: //contagem de caracteres
        quadroEnquadrado = CamadaDeEnlaceReceptoraDesenquadramentoContagemDeCaracteres(quadro);
        break;
      case 2: //insercao de bytes
        quadroEnquadrado =
          CamadaDeEnlaceReceptoraDesenquadramentoInsercaoDeBytes(quadro);
        break;
      case 3:
        quadroEnquadrado = CamadaDeEnlaceReceptoraDesenquadramentoInsercaoDeBits(quadro);
        break;
      case 4: //violacao da camada fisica
        quadroEnquadrado =
          violacaoCamadaFisica(quadro);
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + tipoDeEnquadramento);
    }
    CamadaDeAplicacaoReceptora.decode(quadroEnquadrado);
  }

  /* ***************************************************************
   * Metodo: CamadaDeEnlaceReceptoraDesenquadramentoContagemDeCaracteres
   * Funcao: Retira a informacao de controle de enquadramento por contagem
   *         de caracteres.
   * Parametros: quadro enquadrado como vetor de inteiros
   * Retorno: quadro com os bits de carga util.
   *************************************************************** */
  private static int[] CamadaDeEnlaceReceptoraDesenquadramentoContagemDeCaracteres(int[] quadroEnquadrado) {
    int deslocQuadro = 7;
    int auxDeslocQuadro = 0;
    int indiceQuadro = 0;
    int deslocQuadroEnquadrado = 7;
    int auxDeslocQuadroEnquadrado = 0;
    int indiceQuadroEnquadrado = 0;
    int bitInfo;
    int qtdCaracteres = MainController.bitsNoQuadro / 8;
    int tamanhoQuadro = qtdCaracteres;
    if (tamanhoQuadro % 3 == 0) {
      if (tamanhoQuadro < 4) {
        tamanhoQuadro = 1;
      } else {
        tamanhoQuadro = qtdCaracteres / 4;
      }
    } else {
      tamanhoQuadro = (qtdCaracteres / 4) + 1;
    }
    int[] quadro = new int[tamanhoQuadro];

    int contagem = 0;
    int caracteresNoQuadro = 0;
    for (int i = 0; i < qtdCaracteres; i++) {
      //definindo a contagem:
      if (i % 4 == 0) {
        if (i != 0) {
          indiceQuadroEnquadrado++;
        }
        contagem = 0;
        deslocQuadroEnquadrado = 7;
        i++;//incrementando pois consumo 1 carac de qtdCarac ao identificar o valor de contagem

        for (int j = 7; j >= 0; j--) {
          int mask = 1;
          contagem += (mask << j & quadroEnquadrado[indiceQuadroEnquadrado]);
          deslocQuadroEnquadrado--;
          auxDeslocQuadroEnquadrado++;
        }
        contagem -= 1;
      } // faco a contagem, coloco os bits no quadro
      for (int k = 0; k < contagem * 8; k++) {//caso o erro no meio ocorra em um dos bits de contagem, terei um erro de index por iterar mais do que deveria.
        if (k > (MainController.bitsNoQuadro - 8)) {
          break;
        }
        if (k % 8 == 0 && k != 0) {
          caracteresNoQuadro++;//se ja iterei sobre 8 bits, tenho um caractere no meu quadro.
          i++;//aqui eu incremento porque eu usei uma iteracao do for, para conseguir sincronizar as duas acoes
        }
        if (k % 8 == 0 && caracteresNoQuadro % 4 == 0 && caracteresNoQuadro != 0) {
          indiceQuadro++;//aumentando o indice do meu quadro sempre que salvar 4 caracteres
          deslocQuadro = 7;//retomando o desloc quadro para ler os bits corretamente
          auxDeslocQuadro = 0;//resetando o auxiliar, para saber quantos deslocamentos fiz
        }
        //controle de deslocQuadro
        if (auxDeslocQuadro % 8 == 0 && auxDeslocQuadro != 0) {
          deslocQuadro += 16;
        }//controle de deslocQuadroEnquadrado
        if (auxDeslocQuadroEnquadrado % 8 == 0) {
          deslocQuadroEnquadrado += 16;
        }
        bitInfo = (quadroEnquadrado[indiceQuadroEnquadrado] & (1 << deslocQuadroEnquadrado)) >> deslocQuadroEnquadrado;
        if (bitInfo == -1) bitInfo = 1;
        quadro[indiceQuadro] |= (bitInfo) << deslocQuadro;
        deslocQuadroEnquadrado--;
        deslocQuadro--;
        auxDeslocQuadroEnquadrado++;
        auxDeslocQuadro++;
        if (k == (contagem * 8) - 1) {//contando o caractere quando for o ultimo a ser adicionado
          caracteresNoQuadro++;
        }
      }
    }
    return quadro;
  }

  /* ***************************************************************
   * Metodo: CamadaDeEnlaceReceptoraDesenquadramentoInsercaoDeBytes
   * Funcao: Retira a informacao de controle de enquadramento por Insercao
   *         de bytes.
   * Parametros: quadro enquadrado como vetor de inteiros
   * Retorno: quadro com os bits de carga util.
   *************************************************************** */
  private static int[] CamadaDeEnlaceReceptoraDesenquadramentoInsercaoDeBytes(int[] quadroEnquadrado) {
    int[] quadro = new int[quadroEnquadrado.length];
    //PROTOCOLO
    int flag = 87;
    int esc = 36;
    //FIM INFO PROTOCOLO
    int deslocQuadro = 7;
    int auxDeslocQuadro = 0;
    int indiceQuadro = 0;
    int deslocQuadroEnquadrado = 7;
    int auxDeslocQuadroEnquadrado = 0;
    int indiceQuadroEnquadrado = 0;
    int qtdCaracteres = contarCaracteresNoVetor(quadroEnquadrado);
    int caracteresNoQuadro = 0;
    for (int i = 0; i < qtdCaracteres; i++) {
      if (i % 4 == 0 && i != 0) {
        indiceQuadroEnquadrado++;
        deslocQuadroEnquadrado = 7;
      }
      int aux = 0;
      for (int j = 7; j >= 0; j--) {
        aux += (quadroEnquadrado[indiceQuadroEnquadrado] & (1 << deslocQuadroEnquadrado));
        deslocQuadroEnquadrado--;
        auxDeslocQuadroEnquadrado++;
      }
      aux = aux >> deslocQuadroEnquadrado + 1;
      if (aux == flag) {//ignora se for flag
        if (auxDeslocQuadroEnquadrado % 8 == 0 && auxDeslocQuadroEnquadrado != 0) {
          deslocQuadroEnquadrado += 16;
        }
      } else {//verifica outra possibilidade

        if (aux == esc) {
          //testar
          if (caracteresNoQuadro % 4 == 0 && i > 1) {
            indiceQuadro++;
            deslocQuadro = 7;
          } else {
            if (auxDeslocQuadro % 8 == 0 && auxDeslocQuadro != 0) {
              deslocQuadro += 16;
            }
          }
          i++;
          if (i % 4 == 0 && i != 0) {
            indiceQuadroEnquadrado++;
            deslocQuadroEnquadrado = 7;
          } else {
            if (auxDeslocQuadroEnquadrado % 8 == 0 && auxDeslocQuadroEnquadrado != 0) {
              deslocQuadroEnquadrado += 16;
            }
          }
          //fim teste
          int fakeFlag = 0;
          for (int j = 7; j >= 0; j--) {
            fakeFlag += (quadroEnquadrado[indiceQuadroEnquadrado] & (1 << deslocQuadroEnquadrado));
            deslocQuadroEnquadrado--;
            auxDeslocQuadroEnquadrado++;
          }
          fakeFlag = fakeFlag >> (deslocQuadroEnquadrado + 1);
          quadro[indiceQuadro] += fakeFlag << (deslocQuadro - 7);
          deslocQuadroEnquadrado += 16;
          deslocQuadro -= 8;
          auxDeslocQuadro += 8;
          caracteresNoQuadro++;
        } else {
          if (caracteresNoQuadro % 4 == 0 && i > 1) {
            indiceQuadro++;
            deslocQuadro = 7;
          } else {
            if (auxDeslocQuadro % 8 == 0 && auxDeslocQuadro != 0) {
              deslocQuadro += 16;
            }
          }
          quadro[indiceQuadro] += aux << deslocQuadro - 7;
          deslocQuadroEnquadrado += 16;
          deslocQuadro -= 8;
          auxDeslocQuadro += 8;
          caracteresNoQuadro++;
        }
      }
    }
    quadro = redimensionarVetor(quadro);
    return quadro;
  }

  /* ***************************************************************
   * Metodo: CamadaDeEnlaceReceptoraDesenquadramentoInsercaoDeBits
   * Funcao: Retira a informacao de controle de enquadramento por Insercao
   *         de bits.
   * Parametros: quadro enquadrado como vetor de inteiros
   * Retorno: quadro com os bits de carga util.
   *************************************************************** */
  private static int[] CamadaDeEnlaceReceptoraDesenquadramentoInsercaoDeBits(int[] quadroEnquadrado) {
    int[] quadro = new int[quadroEnquadrado.length];
    int deslocQuadroEnquadrado = 0;
    int indexQuadroEnquadrado = 0;
    int deslocQuadro = 0;
    int indexQuadro = 0;
    int contadorBits = 0;
    int parada;
    String aux = "";

    for (int i = 0; i < quadroEnquadrado.length * 32; i++) {
      if (i % 8 == 0 && i != 0) {
        parada = (quadroEnquadrado[indexQuadroEnquadrado] >> (deslocQuadroEnquadrado)) & 255;
        if (parada == 0) {
          break;
        }
      }

      int bitInfo = (quadroEnquadrado[indexQuadroEnquadrado] & (1 << deslocQuadroEnquadrado)) >> deslocQuadroEnquadrado;
      if (bitInfo == -1) {
        bitInfo = 1;
      }

      aux += bitInfo;
      deslocQuadroEnquadrado++;

      if (deslocQuadroEnquadrado == 32) {
        indexQuadroEnquadrado++;
        deslocQuadroEnquadrado = 0;
      }

      if (bitInfo == 1) {
        contadorBits++;
        if (contadorBits == 5) {
          int bit = (quadroEnquadrado[indexQuadroEnquadrado] & (1 << deslocQuadroEnquadrado)) >> deslocQuadroEnquadrado;
          if (bit == 1 || bit == -1) {
            aux = "";
            contadorBits = 0;
            deslocQuadroEnquadrado++;
            i += 1;
            if (deslocQuadroEnquadrado == 32) {
              indexQuadroEnquadrado++;
              deslocQuadroEnquadrado = 0;
            }
            deslocQuadroEnquadrado++;
            i += 1;
            if (deslocQuadroEnquadrado == 32) {
              indexQuadroEnquadrado++;
              deslocQuadroEnquadrado = 0;
            }
          }
          if (bit == 0) {
            deslocQuadroEnquadrado++;
            i++;
            if (deslocQuadroEnquadrado == 32) {
              indexQuadroEnquadrado++;
              deslocQuadroEnquadrado = 0;
            }
          }
        }
      } else {
        contadorBits = 0;
      }
      if (aux.length() == 8) {
        for (int j = 0; j < 8; j++) {
          quadro[indexQuadro] |= Character.getNumericValue(aux.charAt(j)) << deslocQuadro;
          deslocQuadro++;
          if (deslocQuadro == 32) {
            indexQuadro++;
            deslocQuadro = 0;
          }
        }
        aux = "";
      }
    }
    quadro = redimensionarVetor(quadro);
    return quadro;
  }

  /* ***************************************************************
   * Metodo: violacaoCamadaFisica
   * Funcao: Retira a informacao de controle de enquadramento por VCF
   * Parametros: quadro enquadrado (neste caso, o quadro so contem
   *             a carga util) como vetor de inteiros
   * Retorno: quadro com os bits de carga util.
   *************************************************************** */
  private static int[] violacaoCamadaFisica(int[] quadro) {
    return quadro;
  }

  /* ***************************************************************
   * Metodo: CamadaDeEnlaceTransmissoraControleDeErro
   * Funcao: Verifica se houve erro baseado no algoritmo de deteccao
   *         escolhido na interface.
   * Parametros: quadro com carga util e bits de controle de erro.
   * Retorno: Sem retorno.
   *************************************************************** */
  static public void CamadaDeEnlaceTransmissoraControleDeErro(int quadro[]) {
    int[] quadroVerificado;
    int algControleDeErro = MainController.getTipoDeControleDeErro();
    if (MainController.getTipoDeEnquadramento() == 4) {
      quadroVerificado = quadro;
      CamadaDeEnlaceReceptoraDesenquadramento(quadroVerificado);
    } else {
      switch (algControleDeErro) {
        case 1:
          quadroVerificado = CamadaDeEnlaceReceptoraControleDeErroBitParidadePar(quadro);
          break;
        case 2:
          quadroVerificado = CamadaDeEnlaceReceptoraControleDeErroBitParidadeImpar(quadro);
          break;
        case 3:
          quadroVerificado = CamadaDeEnlaceReceptoraControleDeErroCRC(quadro);
          break;
        case 4:
          quadroVerificado = CamadaDeEnlaceReceptoraControleDeErroCodigoHamming(quadro);
          break;
        default:
          throw new IllegalStateException("Unexpected value: " + algControleDeErro);
      }
      if (quadroVerificado == null) {
        MainController.aplicacaoReceptora.exibeMensagem("ERRO");
      } else {
        CamadaDeEnlaceReceptoraDesenquadramento(quadroVerificado);
      }
    }
  }

  /* ***************************************************************
   * Metodo: CamadaDeEnlaceReceptoraControleDeErroBitParidadePar
   * Funcao: Verifica se houve erro baseado no algoritmo de BitParidade
   *         Par
   * Parametros: quadro com carga util e bits de controle de erro.
   * Retorno: quadro com os bits de carga util ou null se foi
   *          detectado erro.
   *************************************************************** */
  static int[] CamadaDeEnlaceReceptoraControleDeErroBitParidadePar(int[] quadro) {
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
   * Metodo: CamadaDeEnlaceReceptoraControleDeErroBitParidadeÍmpar
   * Funcao: Verifica se houve erro baseado no algoritmo de BitParidade
   *         Ímpar
   * Parametros: quadro com carga util e bits de controle de erro.
   * Retorno: quadro com os bits de carga util ou null se foi
   *          detectado erro.
   *************************************************************** */
  static int[] CamadaDeEnlaceReceptoraControleDeErroBitParidadeImpar(int[] quadro) {
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
      System.out.println("ERRO DETECTADO!");
      return null;
    }
  }

  /* ***************************************************************
   * Metodo: CamadaDeEnlaceReceptoraControleDeErroCRC
   * Funcao: Verifica se houve erro baseado no algoritmo de CRC
   * Parametros: quadro com carga util e bits de controle de erro.
   * Retorno: quadro com os bits de carga util ou null se foi
   *          detectado erro.
   *************************************************************** */
  private static int[] CamadaDeEnlaceReceptoraControleDeErroCRC(int[] quadroControleErro) {
    int[] quadro = new int[quadroControleErro.length - 1];
    StringBuilder crc32Polinomio = new StringBuilder("100000100110000010001110110110111");
    StringBuilder mensagem = new StringBuilder();
    int fim = MainController.bitsNoQuadro;
    int desloc = 0;
    int index = 0;
    for (int i = 0; i < fim; i++) {
      if (i % 32 == 0 && i != 0) {
        index++;
        desloc = 0;
      }
      int bit = (quadroControleErro[index] & 1 << desloc) >> desloc;
      mensagem.append(Math.abs(bit));
      desloc++;
    }
    mensagem.reverse();
    if (calcularCRC32(mensagem.toString(), crc32Polinomio.toString()) != 0) {
      System.out.println("ERRO DETECTADO");
      return null;
    } else {
      StringBuilder mensagemSemCE = new StringBuilder(mensagem.toString().substring(0, mensagem.length() - crc32Polinomio.length() + 1));
      index = 0;
      desloc = 0;

      for (int i = mensagemSemCE.length() - 1; i >= 0; i--) {
        if (desloc == 32) {
          index++;
          desloc = 0;
        }
        int bit = mensagemSemCE.charAt(i) - '0'; // Converte '0' ou '1' para 0 ou 1
        quadro[index] |= (bit << desloc);
        desloc++;
      }
      MainController.bitsNoQuadro -= 32;
      return quadro;
    }
  }

  /* ***************************************************************
   * Metodo: calcularCRC32
   * Funcao: Calcula o resto da divisao dos dados pelo polinomio gerador
   * Parametros: String com bits recebidos e String com os bits do polinomio
   *             gerador
   * Retorno: long com o resultado da divisao.
   *************************************************************** */
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
   * Metodo: CamadaDeEnlaceReceptoraControleDeErroCodigoHamming
   * Funcao: Verifica se houve erro baseado no algoritmo de Codigo
   *         Hamming
   * Parametros: quadro com carga util e bits de controle de erro.
   * Retorno: quadro com os bits de carga util ou null se foi
   *          detectado erro.
   *************************************************************** */
  public static int[] CamadaDeEnlaceReceptoraControleDeErroCodigoHamming(int[] quadro) {
    StringBuilder mensagemCodificada = new StringBuilder();
    int fim = MainController.bitsNoQuadro;
    int desloc = 0;
    int index = 0;

    // Colocando os bits do quadro em uma string
    for (int i = 0; i < fim; i++) {
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
    // fim da extracao dos bits.

    int totalBits = mensagemCodificada.length();
    int r = 0;//quantidade de bits de controle
    while (Math.pow(2, r) < (totalBits + 1)) {//determinando quantos bits de controle sera necessario dado o tamanho da mensagem.
      r++;
    }
    char[] hammingCode = mensagemCodificada.toString().toCharArray();//traansformando a mensagem codificada em um vetor de char para calcular a paridade.

    // Verifica a paridade e identifica a posição do erro (se houver)
    boolean erro = false;
    for (int i = 0; i < r; i++) {
      int parityPos = (int) Math.pow(2, i) - 1;
      boolean paridade = CalcularBirParidade(hammingCode, parityPos);
      if (!paridade) {
        erro = true;
        break;
      }
    }
    if (erro) {
      return null;
    }
    // Caso nao tenha erro, remover os bits de controle.
    StringBuilder mensagemSemParidade = new StringBuilder();
    for (int i = 1; i <= totalBits; i++) {
      if ((i & (i - 1)) != 0) {
        mensagemSemParidade.append(hammingCode[i - 1]);
      }
    }
    //instanciar o vetor para o quadro sem o CE com base no tamanho da mensagem sem os bits de controle.
    int m = mensagemSemParidade.length();
    int tam = m % 32 == 0 ? m / 32 : (m / 32 + 1);
    int[] quadroDecodificado = new int[tam];
    index = 0;
    desloc = 0;
    for (int i = mensagemSemParidade.length() - 1; i >= 0; i--) {
      if (desloc == 32) {
        index++;
        desloc = 0;
      }
      int bit = mensagemSemParidade.charAt(i) - '0'; // Converte '0' ou '1' para 0 ou 1
      quadroDecodificado[index] |= (bit << desloc);
      desloc++;
    }
    MainController.bitsNoQuadro = mensagemSemParidade.length();
    return quadroDecodificado;
  }

  /* ***************************************************************
   * Metodo: CalcularBirParidade
   * Funcao: Calcula a paridade dos bits controlados por um bit de controle
   *         em uma posicao.
   * Parametros: vetor de char contendo os bits recebidos e a posicao do
   *             bit de controle a ser verificado.
   * Retorno: boolean denotando se é par ou não.
   *************************************************************** */
  private static boolean CalcularBirParidade(char[] codigoHamming, int posicaoBit) {
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


  static int contarCaracteresNoVetor(int[] vetor) {
    int indice = 0;
    int deslocBit = 7;
    int flag;
    int fim = vetor.length * 32;
    for (int i = 0; i < vetor.length * 32; i += 8) {
      if (i % 32 == 0 && i != 0) {
        indice++;
        deslocBit = 7;
      }
      if (deslocBit == 7) {
        flag = (vetor[indice]) & 255;
      } else {
        flag = (vetor[indice] >> (deslocBit - 7)) & 255;
      }
      if (flag == 0) {
        fim = i;
        break;
      }
      deslocBit += 8;
    }
    return fim / 8;
  }

  static int[] redimensionarVetor(int vetorOriginal[]) {
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
