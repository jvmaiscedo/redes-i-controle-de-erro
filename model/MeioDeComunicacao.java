/* ***************************************************************
 * Autor............: Joao Victor Gomes Macedo
 * Matricula........: 202210166
 * Inicio...........: 26/03/2024
 * Ultima alteracao.: 01/06/2024
 * Nome.............: MeioDeComunicacao
 * Funcao...........: Encaminha os dados do ponto A ao ponto B
 *                    simulando os meios guiados ou nao guiados.
 *************************************************************** */
package model;

import controller.MainController;
import javafx.application.Platform;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;

import java.util.ArrayList;

public class MeioDeComunicacao {
  private Slider velocidade;
  private TextArea bitsEnviados;
  private TextArea bitsRecebidos;
  private ImageView erroNoMeio;

  public MeioDeComunicacao(Slider velocidade, TextArea bitsEnviados, TextArea bitsRecebidos, ImageView erroNoMeio) {
    this.velocidade = velocidade;
    this.bitsEnviados = bitsEnviados;
    this.bitsRecebidos = bitsRecebidos;
    this.erroNoMeio = erroNoMeio;
  }

  /* ***************************************************************
   * Metodo: recebe
   * Funcao: Recebe o fluxo de bits e envia do ponto A ao ponto B
   * Parametros: vetor de inteiros
   * Retorno: sem retorno
   *************************************************************** */
  public void recebe(int[] fluxo) {//processando o fluxo recebido.
    int[] fluxoBrutoDeBitsPontoA;
    int[] fluxoBrutoDeBitsPontoB = new int[fluxo.length];
    fluxoBrutoDeBitsPontoA = fluxo;
    new Thread(() -> {
      int desloc = 0;
      int index = 0;
      int numeroPseudoAleatorio = (int) (Math.random() * 10) + 1;
      int taxaDeErro = MainController.getTaxaErro();
      int qtdDeErro = MainController.getQtdDeBitsErro();
      ArrayList<Integer> posicoes = new ArrayList<>();
      if (taxaDeErro > 0) {
        calcularPosicoesDoErro(qtdDeErro, posicoes);
      }
      Platform.runLater(() -> {
        bitsEnviados.setText(MainController.textArea(MainController.bitsNoQuadro, fluxoBrutoDeBitsPontoA));
      });
      for (int i = 0; i < MainController.bitsNoQuadro; i++) {
        if (i % 32 == 0 && i != 0) {
          desloc = 0;
          index++;
        }
        int bit = (fluxoBrutoDeBitsPontoA[index] & 1 << desloc) >> desloc;
        bit = Math.abs(bit);
        if (numeroPseudoAleatorio <= taxaDeErro && posicoes.contains(i)) {
          Platform.runLater(() -> {
            erroNoMeio.setVisible(true);
          });
          if (MainController.getTipoDeCodificacao() == 2) {
            if (bit == 1) {
              fluxoBrutoDeBitsPontoB[index] &= ~(1 << desloc);
              fluxoBrutoDeBitsPontoB[index] |= 1 << (desloc - 1);
            } else {
              fluxoBrutoDeBitsPontoB[index] |= 1 << desloc;
              fluxoBrutoDeBitsPontoB[index] &= ~(1 << (desloc - 1));
            }
          } else {
            if (bit == 1) {
              fluxoBrutoDeBitsPontoB[index] |= 0 << desloc;
            } else {
              fluxoBrutoDeBitsPontoB[index] |= 1 << desloc;
            }
          }
        } else {
          fluxoBrutoDeBitsPontoB[index] |= bit << desloc;
        }
        MainController.atualizarOnda();
        MainController.atualizaPrimeiroSinal(bit, i);
        desloc++;
        try {
          Thread.sleep((long) velocidade.getValue() * 10); // sleep para a interface grafica atualizar
        } catch (InterruptedException e) {
        }
      }
      for (int k = 7; k >= 0; k--) {
        MainController.atualizarOnda();
        MainController.atualizaPrimeiroSinal(-1, k);
        try {
          Thread.sleep((long) velocidade.getValue() * 10); // sleep para a interface grafica atualizar
        } catch (InterruptedException e) {
        }
      }
      Platform.runLater(() -> {
        bitsRecebidos.setText(MainController.textArea(MainController.bitsNoQuadro, fluxoBrutoDeBitsPontoB));
      });
      try {
        Thread.sleep(10); // sleep para a interface grafica atualizar
      } catch (InterruptedException e) {
      }
      Platform.runLater(() -> {
        erroNoMeio.setVisible(false);
      });
      CamadaFisicaReceptora.decodificaQuadro(fluxoBrutoDeBitsPontoB);
      MainController.quadroEnviado.release();
    }).start();
  }

  /* ***************************************************************
   * Metodo: calcularPosicoesDoErro
   * Funcao: calcula posicoes pseudoaleatorias em que ocorrera um erro.
   * Parametros: quantidade de bits e array de posicoes
   * Retorno: sem retorno
   *************************************************************** */
  public void calcularPosicoesDoErro(int qtdErro, ArrayList<Integer> posicoes) {
    int indice;
    for (int i = 0; i < qtdErro; i++) {
      indice = (int) (Math.random() * MainController.bitsNoQuadro + 1);
      if (MainController.getTipoDeCodificacao() == 1) {
        posicoes.add(indice);
      } else {
        while (indice % 2 == 0) {
          indice = (int) (Math.random() * MainController.bitsNoQuadro + 1);
        }
        posicoes.add(indice);
      }
    }
  }

}
