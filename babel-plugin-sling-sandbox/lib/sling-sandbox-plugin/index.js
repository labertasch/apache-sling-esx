module.exports = function (babel) {
  const { types: t } = babel;
  
  return {
    visitor: {
        BlockStatement(path) {
		var threadInterrupted =  t.callExpression(
                    t.memberExpression(
                        t.identifier('SlingSandbox'), 
                        t.identifier('checkInterrupted'))
                , []); 
          
          path.unshiftContainer('body', 
			t.expressionStatement(
				threadInterrupted
            )                                                              );
        }
    }
  };
}