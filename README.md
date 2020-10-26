# coc-analyser

Análise de Guerra e Liga Clash of Clans - COC

Sempre adicionar token (coc.token) de api de desenvolvedor no arquivo de properties application.properties;

Na execução adicionar o path dos arquivo: application.properties(onde é configurado os score) e strings.properties(onde se configura as strings da planilha). E como args a lista de clãs que se deseja fazer a análise.

Exemplo de execução:
java -Dcoc.app.properties.file=file:/d:/COC/application.properties -Dcoc.app.strings.file=file:/d:/COC/strings.properties -jar D:\COC\coc-2.0.2020-10.jar #909GVXXX #808GVXXX

Gerar token a partir de https://developer.clashofclans.com/
