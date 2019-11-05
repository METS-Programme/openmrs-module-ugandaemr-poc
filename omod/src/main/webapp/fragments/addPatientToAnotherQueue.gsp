<div class="modal fade" id="add_patient_to_other_queue_dialog" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel"
     aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                Send Patient To Another Location <span id="checkin_patient_names"></span><i class="icon-check medium"></i>
            </div>

            <div class="modal-body">
                <div class="container">
                    <input type="hidden" id="patient_id" name="patient_id" value="">

                    <div class="row">
                        <div class="col-5">Next Service Point:</div>

                        <div class="col-7">
                            <div class="form-group">
                                <select class="form-control" name="location_id" id="location_id">
                                </select>
                                <span class="field-error" style="display: none;"></span>
                                <div id="error_location_id">${ui.message("patientqueueing.select.error")}</div>
                            </div>
                        </div>
                    </div>

                    <div class="row" id="patient_status_container">
                        <div class="col-4">Urgency of Care:</div>

                        <div class="col-8">
                            <div class="form-group">
                                <select class="form-control" id="patient_status" name="patient_status">
                                    <option value="">Select Urgency</option>
                                    <option value="non-emergency">Non-Emergency</option>
                                    <option value="emergency">Emergency</option>
                                </select>
                                <span class="field-error" style="display: none;"></span>
                            </div>
                        </div>
                    </div>

                    <div class="row" id="visit_comment_container">
                        <div class="col-4">Visit Type:</div>

                        <div class="col-8">
                            <div class="form-group">
                                <select class="form-control" id="visit_comment" name="visit_comment">
                                    <option value="">Select Visit Type</option>
                                    <option value="new visit">New Visit</option>
                                    <option value="revisit">Revisit</option>
                                </select>
                                <span class="field-error" style="display: none;"></span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>


            <div class="modal-footer form">
                <button type="button" class="btn btn-danger" data-dismiss="modal">Cancel</button>
                <input type="submit" class="confirm" id="checkin" value="Check In">
            </div>
        </div>
    </div>
</div>