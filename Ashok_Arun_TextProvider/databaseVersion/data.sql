CREATE TABLE lines(
  lineNum SERIAL PRIMARY KEY,
  lineText TEXT
);
-- enter the path in the quotes
COPY lines (lineText) FROM 'PATH';