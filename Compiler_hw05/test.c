int add(int x, int y){
	int z;
	z = x + y;
	return z;
}

int sum(int n){
	int result = 0;
	int i = 1;
	while(i <= n){
		result = result + i;
		i = i + 1;
	}
	return result;
}

int simpleif(int x, int y){
	if(x >= y){
		--x;
		return x;
	}else{
		--y;
	}
	return y;
}

int ifstmt(int x, int y){
	if(x == y){
		--y;
	}else if(x + y < 10){
		x = y + x + 1;
		return x;
	}else if(!(x * y < y) and y <= 0){
		y = y % x;
	}else if(x == y or -x > y){
		return y / x;
	}else{
	}
	return y;
}

void main () {
	int t = 33;
	int n = 100; 
	_print(add(1, t));
	_print(sum(n));
	_print(simpleif(t, n));
	_print(ifstmt(t, n));
}