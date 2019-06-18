select (select count(*) from orders), (select count(*) from reservations), (select count(*) from transactions);
select (select count(*) from orders where status != 'COMPLETE') not_complete, (select count(*) from reservations where status != 'CONFIRMED') not_confirmed, (select count(*) from transactions where status != 'AUTHORIZED') not_authorized;
select (select count(*) from orders where status = 'COMPLETE') complete, (select count(*) from reservations where status = 'CONFIRMED') confirmed, (select count(*) from transactions where status = 'AUTHORIZED') authorized;

delete from orders;
delete from reservations;
delete from transactions;

select (select count(*) from order_events), (select count(*) from reservation_events), (select count(*) from transaction_events);

select o.*, r.*, t.* from orders o left outer join reservations r on o.id = r.order_id left outer join transactions t on o.id = t.order_id where o.status = 'NEW';
select r.*, t.* from reservations r left outer join transactions t on r.order_id = t.order_id where r.status = 'PENDING';
select o.*, r.*, t.* from orders o left outer join reservations r on o.id = r.order_id left outer join transactions t on o.id = t.order_id where (o.status = 'NEW' or r.status = 'PENDING');

select count(*) from orders o inner join reservations r on o.id = r.order_id inner join transactions t on o.id = t.order_id;
select count(*) from (select o.id from orders o inner join reservations r on o.id = r.order_id inner join transactions t on o.id = t.order_id group by o.id) as rs;
select o.id, count(*) from orders o inner join reservations r on o.id = r.order_id inner join transactions t on o.id = t.order_id group by o.id having count(*) > 1;
