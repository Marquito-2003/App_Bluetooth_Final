Controle Bluetooth com Android e Arduino

📖 Descrição

Este projeto consiste em um aplicativo Android que se comunica com um módulo Bluetooth HC-06 para enviar comandos a um Arduino. Ele permite listar dispositivos Bluetooth, conectar-se ao HC-06 e enviar comandos de controle.

Autores:

•Marco Aurélio Gonçalves Fonseca - 221021509

•Marcos Paulo Siqueira Borges - 221021518

🛠️ Tecnologias Utilizadas

- Linguagem: Java (Android)
- Interface: XML (Android Layouts)
- Comunicação: Bluetooth
- Hardware: Módulo Bluetooth HC-06, Arduino

## 📂 Estrutura do Projeto


📂 ProjetoBluetooth

 ┣ 📜 MainActivity2.java         # Envio de comandos via Bluetooth
 
 ┣ 📜 ListaDispositivos.java     # Lista dispositivos Bluetooth e permite conexão
 
 ┣ 📜 AndroidManifest.xml       # Permissões e configurações do aplicativo
 
 ┣ 📜 activity_main.xml         # Layout da interface do usuário
 
 ┗ 📜 README.md                 # Documento explicativo do projeto


📋 Pré-requisitos

Antes de executar o projeto, certifique-se de que:

- Um dispositivo Android com suporte a Bluetooth esteja disponível.
- O módulo HC-06 esteja configurado corretamente com o Arduino.

🔧 Configuração e Execução

1. Clone o repositório:
   https://github.com/Marquito-2003/App_Bluetooth_Final.git
2. Abra o projeto no Android Studio.
3. Conecte seu dispositivo Android ou use um emulador com suporte a Bluetooth.
4. Conceda permissões de Bluetooth ao aplicativo.
5. Compile e execute o aplicativo.

🚀 Funcionalidades

- Buscar dispositivos Bluetooth disponíveis.
- Conectar ao módulo HC-06.
- Enviar comandos via Bluetooth para o Arduino.
- Interface amigável para controle dos dispositivos.

⚙️ Modificações Importantes

- MainActivity2.java: Implementa a lógica para conexão e envio de comandos Bluetooth.
- ListaDispositivos.java: Permite ao usuário visualizar e selecionar dispositivos Bluetooth.
- AndroidManifest.xml: Adicionadas permissões para uso do Bluetooth.
- activity\_main.xml: Ajustado layout para melhor experiência do usuário.

📜 Licença

Este projeto está sob domínio do Grupo Ereko - UnB



