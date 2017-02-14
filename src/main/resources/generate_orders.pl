use strict;
use warnings;

use Data::Dumper;

die "need to pass three parameters!" unless $#ARGV == 2;

my ($case_limit, $paints_limit, $file_name) = @ARGV;

open FH,">",$file_name;


my $case_num = int(rand($case_limit)) + 1; 
print FH  $case_num, "\n";
for (my $i = 0; $i < $case_num; $i++) {
    my $cust_num = int(rand($paints_limit)) + 1;
    my $paints_num = int(rand($paints_limit)) + 1;
    print FH $paints_num,"\n";
    print FH $cust_num, "\n";
    my $next = 1;
    for (my $j = 0; $j < $cust_num; $j++) {
	my $order_length = int(rand($paints_num)) + 1;
	my $matte_num = int(rand($paints_num)) + 1;
	my $ordered_paints = {};
	my $result = [];
	push @$result, $order_length;
	push @$result, $matte_num;
	my $next = int(rand(10)) % 2 ? 1 : 0;
	push @$result, $next;
	$ordered_paints->{$matte_num} = 1;
	for (my $k = 1; $k < $order_length; $k++) {
	    my $paint = int(rand($paints_num - 1)) + 1;
	    unless (defined $ordered_paints->{$paint}) {
		$ordered_paints->{$paint} = 1;
		push @$result, $paint, 0;
	    }
	}
	my $order_actual_length = $#$result / 2;
	$result->[0] = $order_actual_length;
	print FH join(" ",@$result),"\n";    
    }
}
close FH;
