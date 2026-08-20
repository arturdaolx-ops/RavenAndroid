# Raven Android

Aplicativo Android único com:
- token Raven salvo localmente;
- ativar/desativar notificações;
- som/vibração;
- opção de mostrar valor;
- consulta periódica da API do Raven;
- notificação de nova venda;
- widget de tela inicial;
- atualização manual.

## Como gerar o APK

### Opção 1 — Android Studio
1. Instale Android Studio.
2. Abra esta pasta como projeto.
3. Aguarde o Gradle sincronizar.
4. Menu Build > Build Bundle(s) / APK(s) > Build APK(s).
5. O APK será gerado em:
   app/build/outputs/apk/debug/app-debug.apk

### Opção 2 — terminal
Com Android SDK e Gradle configurados:
./gradlew assembleDebug

O APK estará em:
app/build/outputs/apk/debug/app-debug.apk

## Observações
- O app consulta a API do Raven a cada 15 minutos usando WorkManager.
- Android pode atrasar tarefas em segundo plano por economia de bateria.
- Para notificações realmente instantâneas, o Raven precisa fornecer webhook/push.
- A exibição na tela de bloqueio é controlada também pelas configurações do sistema Android.
