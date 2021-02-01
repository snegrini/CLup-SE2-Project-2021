$(function() {
    setInterval(update, 10000);
});

function update() {
    $.post( "dashboard/employee", function(data) {

        var $customersInside = $("#customersInside");
        var $customersQueue = $("#customersQueue");
        var $storeCap = $("#storeCap");

        $customersInside.text(data.customersInside);
        $customersQueue.text(data.customersQueue);
        $storeCap.text(data.storeCap);

        var $table_obj = $("<table>");
        var $tr = $("<tr>");

        $tr.append($("<th>").text("Pass code"))
            .append($("<th>").text("Pass status"))
            .append($("<th>").text("Queue number"))
            .append($("<th>").text("Arrival time"))
        $tr.appendTo($table_obj);

        if (!data.tickets.length) {
            $("#divPassCode").html($("<div>").text("Wow, such empty"));
        } else {
            $.each(data.tickets,function(i, ticket) {
                $("<tr>").append($("<td>").text(ticket.passCode))
                    .append($("<td>").text(ticket.passStatus))
                    .append($("<td>").text(ticket.queueNumber))
                    .append($("<td>").text(ticket.arrivalTime))
                    .appendTo($table_obj);
            });
            $("#divPassCode table:first").remove();
            $("#divPassCode").html($table_obj);
        }

    });
}
