package com.jackie.bluetooth;

public class Mensagem {

    private String dado1;
    private String dado2;
    private String dado3;
    private String dado4;

        public Mensagem(String dado1, String dado2, String dado3, String dado4 ) {
            this.dado1 = dado1;
            this.dado2 = dado2;
            this.dado3 = dado3;
            this.dado4 = dado4;

        }

        public String getDado1() {
            return dado1;
        }

        public void setDado1(String dado1) {
            this.dado1 = dado1;
        }

        public String getDado2() {
            return dado2;
        }

        public void setDado2(String dado2) {
            this.dado2 = dado2;
        }

        public String getDado3() {
            return dado3;
        }

        public void setDado3(String dado3) {
            this.dado3 = dado3;
        }
        public String getDado4() {
            return dado4;
        }

        public void setDado4(String dado4) {
            this.dado4 = dado4;
        }

        @Override
        public String toString() {
            return "Dado1: " + dado1 + " Dado2: " +
                    dado2 + " Dado3: " + dado3 + " Dado4: " + dado4;
        }
}
