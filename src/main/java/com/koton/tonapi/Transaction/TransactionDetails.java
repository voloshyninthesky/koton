package com.koton.tonapi.Transaction;

import java.util.ArrayList;

public class TransactionDetails{
	public String account;
	public long lt;
	public String hash;
	public Long utime;
	public Long fee;
	public Long storage_fee;
	public Long other_fee;
	public String transaction_type;
	public Object compute_skip_reason;
	public Long compute_exit_code;
	public Long compute_gas_used;
	public Long compute_gas_limit;
	public Long compute_gas_credit;
	public Long compute_gas_fees;
	public Long compute_vm_steps;
	public Long action_result_code;
	public Long action_total_fwd_fees;
	public Long action_total_action_fees;
	public InMsg in_msg;
	public ArrayList<OutMsg> out_msgs;

	@Override
	public String toString() {
		return "TransactionDetails{" +
				"account='" + account + '\'' +
				", lt=" + lt +
				", hash='" + hash + '\'' +
				", utime=" + utime +
				", fee=" + fee +
				", storage_fee=" + storage_fee +
				", other_fee=" + other_fee +
				", transaction_type='" + transaction_type + '\'' +
				", compute_skip_reason=" + compute_skip_reason +
				", compute_exit_code=" + compute_exit_code +
				", compute_gas_used=" + compute_gas_used +
				", compute_gas_limit=" + compute_gas_limit +
				", compute_gas_credit=" + compute_gas_credit +
				", compute_gas_fees=" + compute_gas_fees +
				", compute_vm_steps=" + compute_vm_steps +
				", action_result_code=" + action_result_code +
				", action_total_fwd_fees=" + action_total_fwd_fees +
				", action_total_action_fees=" + action_total_action_fees +
				", in_msg=" + in_msg +
				", out_msgs=" + out_msgs +
				'}';
	}
}
