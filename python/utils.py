def generate_cell(N, E, coef=0.04166666666667):
    from decimal import Decimal

    if (N==None or N=='None' or E==None or E=='None'):
        return None

    if coef<0.04166666666667: coef = 0.04166666666667
    if coef>1: coef = 1
    row = int(round(Decimal(Decimal(90.0)+Decimal(N))/Decimal(coef)))
    col = int(round(Decimal(Decimal(180.0)+Decimal(E))/Decimal(coef)))
    key = str(row)+'_'+str(col)

    return key

def get_words_in_string(inputString):
    from re import findall
    return findall(r'(?u)\w+', inputString)
