/* ***************************************************************
 * Autor............: Joao Victor Gomes Macedo
 * Matricula........: 202210166
 * Inicio...........: 26/03/2024
 * Ultima alteracao.: 07/04/2024
 * Nome.............: Aplicacao Transmissora
 * Funcao...........: Encaminha os dados que serao codificados para
 *                    a camada de Aplicacao Transmissora.
 *************************************************************** */
package model;

public class AplicacaoTransmissora {
  /* ***************************************************************
   * Metodo: enviarMensagem
   * Funcao: Envia a mensagem para a formacao do quadro de bits.
   * Parametros: String contendo a mensagem.
   * Retorno: Sem retorno.
   *************************************************************** */
  public static void enviarMensagem(String mensagem) {
    CamadaDeAplicacaoTransmissora.gerarQuadro(mensagem);
  }
}
