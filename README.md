# PostTag
Process rules:
  All chars are 0 or 1.                                                             
  antecedentString -> consequentString                                             
  
  0abcde -> cde00    //String begins with '0' character, append 00 then delete first three characters.
  1abcde -> cde1101  //String begins with '1' character, append 1101 then delete first three characters.
  
  Iterate until consequentString repeats a previous string creating an infinite oscillation or string shrinks to null string.
