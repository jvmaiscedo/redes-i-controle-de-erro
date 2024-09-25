/* ***************************************************************
 * Autor............: Joao Victor Gomes Macedo
 * Matricula........: 202210166
 * Inicio...........: 26/03/2024
 * Ultima alteracao.: 01/06/2024
 * Nome.............: AplicacaoReceptora
 * Funcao...........: Recebe a mensagem e exibe ao usuario.
 *************************************************************** */
package model;

import controller.MainController;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;

public class AplicacaoReceptora {
  private TextArea text;//Elemento grafico que sera utilizado pela classe.
  private ImageView erro;//Elemento grafico caso detecte erro.

  public AplicacaoReceptora(TextArea textArea, ImageView erro) {
    this.text = textArea;
    this.erro = erro;
  }

  /* ***************************************************************
   * Metodo: exibeMensagem
   * Funcao: exibe a mensagem na tela para o usuarios
   * Parametros: String contendo a mensagem.
   * Retorno: Sem retorno.
   *************************************************************** */
  public void exibeMensagem(String mensagem) {
    if (mensagem.equals("ERRO")) {
      MainController.erroDetectado = true;
      Platform.runLater(() -> erro.setVisible(true));
      MainController.mensagemFinal = "";
      MainController.mensagemCompleta = true;
    } else {
      MainController.mensagemFinal += mensagem;
    }
    if (MainController.mensagemCompleta) {
      Platform.runLater(() -> {
        text.setText(MainController.mensagemFinal);
      });
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      MainController.mensagemFinal = "";//limpando a mensagem para que a proxima mensagem seja exibida corretamente
      MainController.bitsNoQuadro = 0;
      MainController.bitsNaMensagem = 0;
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
