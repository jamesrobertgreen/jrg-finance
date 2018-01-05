(function (ModalUtils, $, undefined) {

    // When the user clicks the button, open the modal
    ModalUtils.openModal = function() {
        var modal = $("#myModal");
        modal.addClass("modal-shown");
        modal.removeClass("modal-hidden");
    }

    // When the user clicks the button, close the modal
    ModalUtils.closeModal = function() {
        var modal = $("#myModal");
        modal.addClass("modal-hidden");
        modal.removeClass("modal-shown");
    }

}(window.ModalUtils = window.ModalUtils || {}, jQuery));
