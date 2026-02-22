FROM eclipse-temurin:17
WORKDIR /app
COPY src ./src
RUN mkdir -p out && find src -name "*.java" | xargs javac -d out
CMD ["java", "-cp", "out", "edu.mum.cs.cs425.demos.studentrecordsmgmtapp.MyStudentRecordsMgmtApp"]
