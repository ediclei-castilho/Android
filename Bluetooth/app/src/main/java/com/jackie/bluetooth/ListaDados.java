package com.jackie.bluetooth;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.jackie.bluetooth.ListaDispositivos.ENDERECO_MAC;

public class ListaDados extends AppCompatActivity {

    static List lista;

    public class ListaDeCursosActivity extends AppCompatActivity {

        /*@Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            ListView lista = (ListView) findViewById(R.id.mensagem);
            List<Mensagem> mensagem = todosOsDados();
            ArrayAdapter<Mensagem> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mensagem);
            lista.setAdapter(adapter);

            Intent retornaMsg = new Intent();
            retornaMsg.putExtra(ENDERECO_MAC, retornaMsg);
            setResult(RESULT_OK, retornaMsg);
            finish();
        }

        /**
         * Exemplo qualquer de devolução de uma lista de cursos.
         * Para esse exemplo será considerado um hard coded.
         *
         * @return lista com todos os cursos

        private List<Mensagem> todosOsDados() {
            return new ArrayList<>(Arrays.asList(
                    new Mensagem("Java", "básico de Java", "bls", "bla"),
                    new Mensagem("HTML e CSS", "HTML 5 e suas novidades", "bls", "bla"),
                    new Mensagem("Android", "boas práticas", "bls", "bla")));
        }*/
    }

}
