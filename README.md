# Практическая работа №5

Вариант 3. Строки, массивы, циклы и методы обработки данных.

## Сборка и запуск

```bash
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out ru.edu.pr02.SelfCheck
```

Windows (PowerShell):

```powershell
mkdir out -Force
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse -Filter *.java src).FullName
java -cp out ru.edu.pr02.SelfCheck
```

## Диаграммы

```bash
dot -Tpng diagrams/activity.dot -o diagrams/activity.png
dot -Tpng diagrams/class.dot   -o diagrams/class.png
```